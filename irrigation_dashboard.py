"""
Smart Irrigation Dashboard (Tkinter + Matplotlib + PySerial)

Reads Arduino Uno serial output like:
  Moisture Value: 650
  Soil Dry -> Pump ON
  ----------------

Updates the UI and scrolling graph in near real-time.
"""

from __future__ import annotations

import argparse
import json
import queue
import re
import threading
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import tkinter as tk
from tkinter import ttk

import matplotlib

matplotlib.use("TkAgg")
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure

import serial
from serial.tools import list_ports


CONFIG_PATH = Path(__file__).with_name("dashboard_config.json")


MOISTURE_RE = re.compile(r"Moisture\s*Value\s*:\s*(\d+)", re.IGNORECASE)
STATUS_RE = re.compile(
    r"Soil\s*(Dry|Wet)\s*->\s*Pump\s*(ON|OFF)", re.IGNORECASE
)


@dataclass(frozen=True)
class Reading:
    ts: float
    moisture: Optional[int] = None
    soil: Optional[str] = None  # "Dry" / "Wet"
    pump: Optional[str] = None  # "ON" / "OFF"
    raw: str = ""


class SerialWorker:
    def __init__(self, out_queue: "queue.Queue[Reading]", log_queue: "queue.Queue[str]"):
        self._out_queue = out_queue
        self._log_queue = log_queue
        self._thread: Optional[threading.Thread] = None
        self._stop = threading.Event()
        self._lock = threading.Lock()
        self._ser: Optional[serial.Serial] = None
        self._connected_port: Optional[str] = None

    @property
    def connected_port(self) -> Optional[str]:
        with self._lock:
            return self._connected_port

    def start(self, port: str, baud: int, timeout: float = 1.0) -> None:
        self.stop()
        self._stop.clear()
        self._thread = threading.Thread(
            target=self._run, args=(port, baud, timeout), daemon=True
        )
        self._thread.start()

    def stop(self) -> None:
        self._stop.set()
        with self._lock:
            if self._ser is not None:
                try:
                    self._ser.close()
                except Exception:
                    pass
            self._ser = None
            self._connected_port = None

    def _log(self, msg: str) -> None:
        try:
            self._log_queue.put_nowait(msg)
        except Exception:
            pass

    def _run(self, port: str, baud: int, timeout: float) -> None:
        self._log(f"Connecting to {port} @ {baud}...")
        try:
            ser = serial.Serial(port=port, baudrate=baud, timeout=timeout)
        except Exception as exc:
            self._log(f"Connection failed: {exc}")
            return

        with self._lock:
            self._ser = ser
            self._connected_port = port

        self._log("Connected.")

        latest_moisture: Optional[int] = None
        latest_soil: Optional[str] = None
        latest_pump: Optional[str] = None

        try:
            while not self._stop.is_set():
                try:
                    raw_bytes = ser.readline()
                except Exception as exc:
                    self._log(f"Serial read error: {exc}")
                    break

                if not raw_bytes:
                    continue

                try:
                    line = raw_bytes.decode("utf-8", errors="ignore").strip()
                except Exception:
                    continue

                if not line or line.startswith("-"):
                    continue

                moisture_match = MOISTURE_RE.search(line)
                if moisture_match:
                    try:
                        latest_moisture = int(moisture_match.group(1))
                    except Exception:
                        latest_moisture = None
                    self._out_queue.put(
                        Reading(
                            ts=time.time(),
                            moisture=latest_moisture,
                            soil=latest_soil,
                            pump=latest_pump,
                            raw=line,
                        )
                    )
                    continue

                status_match = STATUS_RE.search(line)
                if status_match:
                    latest_soil = status_match.group(1).capitalize()
                    latest_pump = status_match.group(2).upper()
                    self._out_queue.put(
                        Reading(
                            ts=time.time(),
                            moisture=latest_moisture,
                            soil=latest_soil,
                            pump=latest_pump,
                            raw=line,
                        )
                    )
                    continue

                # Unknown line; forward as raw for debug/logging.
                self._out_queue.put(Reading(ts=time.time(), raw=line))
        finally:
            with self._lock:
                try:
                    ser.close()
                except Exception:
                    pass
                self._ser = None
                self._connected_port = None
            self._log("Disconnected.")


class DashboardApp:
    def __init__(self, root: tk.Tk, *, port: Optional[str], baud: int, interval_ms: int):
        self.root = root
        self.root.title("Smart Irrigation Dashboard")
        self.root.minsize(1100, 650)

        self.interval_ms = interval_ms
        self.baud = baud

        self.readings_queue: "queue.Queue[Reading]" = queue.Queue()
        self.log_queue: "queue.Queue[str]" = queue.Queue()
        self.serial_worker = SerialWorker(self.readings_queue, self.log_queue)

        self._series_capacity = 240  # ~2 minutes at 500ms
        self._t0 = time.time()
        self._x: list[float] = []
        self._y: list[int] = []

        self._latest_moisture: Optional[int] = None
        self._latest_soil: str = "—"
        self._latest_pump: str = "—"
        self._connected: bool = False

        self._build_theme()
        self._build_ui()

        self._load_config()
        if port:
            self.port_var.set(port)
        self.baud_var.set(str(baud))

        self._refresh_ports()
        self._set_connection_state(False, detail="Not connected")

        self.root.protocol("WM_DELETE_WINDOW", self._on_close)
        self._poll_queues()
        self._schedule_graph_refresh()

    # ---------- Theme / UI ----------
    def _build_theme(self) -> None:
        self.colors = {
            "bg": "#0b1f14",
            "panel": "#0f2a1b",
            "panel2": "#123322",
            "text": "#e9fff2",
            "muted": "#b6e8c8",
            "accent": "#3ddb6a",
            "accent2": "#24c75a",
            "warn": "#ffb020",
            "danger": "#ff4d4d",
            "grid": "#1d4a32",
            "card": "#102a1c",
        }

        style = ttk.Style()
        try:
            style.theme_use("clam")
        except Exception:
            pass

        self.root.configure(bg=self.colors["bg"])

        style.configure(
            "TFrame",
            background=self.colors["bg"],
        )
        style.configure(
            "Panel.TFrame",
            background=self.colors["panel"],
        )
        style.configure(
            "Card.TFrame",
            background=self.colors["card"],
        )
        style.configure(
            "TLabel",
            background=self.colors["bg"],
            foreground=self.colors["text"],
            font=("Segoe UI", 11),
        )
        style.configure(
            "Title.TLabel",
            font=("Segoe UI Semibold", 18),
            foreground=self.colors["text"],
            background=self.colors["bg"],
        )
        style.configure(
            "SubTitle.TLabel",
            font=("Segoe UI", 10),
            foreground=self.colors["muted"],
            background=self.colors["bg"],
        )
        style.configure(
            "CardTitle.TLabel",
            font=("Segoe UI Semibold", 12),
            foreground=self.colors["muted"],
            background=self.colors["card"],
        )
        style.configure(
            "Value.TLabel",
            font=("Segoe UI Semibold", 48),
            foreground=self.colors["accent"],
            background=self.colors["card"],
        )
        style.configure(
            "BigStatus.TLabel",
            font=("Segoe UI Semibold", 20),
            background=self.colors["card"],
        )
        style.configure(
            "TButton",
            font=("Segoe UI Semibold", 10),
            padding=(10, 6),
        )
        style.map(
            "TButton",
            background=[("active", self.colors["panel2"])],
        )
        style.configure(
            "Accent.TButton",
            background=self.colors["accent2"],
            foreground="#062010",
        )
        style.map(
            "Accent.TButton",
            background=[("active", self.colors["accent"])],
        )
        style.configure(
            "TEntry",
            fieldbackground=self.colors["panel2"],
            background=self.colors["panel2"],
            foreground=self.colors["text"],
            insertcolor=self.colors["text"],
        )
        style.configure(
            "TCombobox",
            fieldbackground=self.colors["panel2"],
            background=self.colors["panel2"],
            foreground=self.colors["text"],
            arrowcolor=self.colors["text"],
        )

    def _build_ui(self) -> None:
        root = self.root

        header = ttk.Frame(root)
        header.pack(fill="x", padx=18, pady=(16, 10))

        title = ttk.Label(header, text="Arduino Smart Irrigation Dashboard", style="Title.TLabel")
        title.pack(anchor="w")

        subtitle = ttk.Label(
            header,
            text="Live soil moisture monitoring • Pump automation status • Real-time trend graph",
            style="SubTitle.TLabel",
        )
        subtitle.pack(anchor="w", pady=(4, 0))

        body = ttk.Frame(root)
        body.pack(fill="both", expand=True, padx=18, pady=(0, 18))

        left = ttk.Frame(body)
        left.pack(side="left", fill="y", padx=(0, 14))

        right = ttk.Frame(body)
        right.pack(side="left", fill="both", expand=True)

        # Connection panel
        conn = ttk.Frame(left, style="Panel.TFrame")
        conn.pack(fill="x", pady=(0, 14))

        conn_title = ttk.Label(conn, text="Connection", style="CardTitle.TLabel", background=self.colors["panel"])
        conn_title.pack(anchor="w", padx=12, pady=(10, 6))

        form = ttk.Frame(conn, style="Panel.TFrame")
        form.pack(fill="x", padx=12, pady=(0, 12))

        self.port_var = tk.StringVar(value="")
        self.baud_var = tk.StringVar(value="9600")

        ttk.Label(form, text="COM Port", background=self.colors["panel"]).grid(row=0, column=0, sticky="w", pady=(0, 6))
        self.port_combo = ttk.Combobox(form, textvariable=self.port_var, values=[], width=18, state="normal")
        self.port_combo.grid(row=1, column=0, sticky="we", padx=(0, 8))

        ttk.Label(form, text="Baud", background=self.colors["panel"]).grid(row=0, column=1, sticky="w", pady=(0, 6))
        self.baud_entry = ttk.Entry(form, textvariable=self.baud_var, width=10)
        self.baud_entry.grid(row=1, column=1, sticky="we")

        form.grid_columnconfigure(0, weight=1)

        buttons = ttk.Frame(conn, style="Panel.TFrame")
        buttons.pack(fill="x", padx=12, pady=(0, 12))

        self.connect_btn = ttk.Button(buttons, text="Connect", style="Accent.TButton", command=self._on_connect)
        self.connect_btn.pack(side="left")

        self.disconnect_btn = ttk.Button(buttons, text="Disconnect", command=self._on_disconnect, state="disabled")
        self.disconnect_btn.pack(side="left", padx=(8, 0))

        self.refresh_btn = ttk.Button(buttons, text="Refresh Ports", command=self._refresh_ports)
        self.refresh_btn.pack(side="left", padx=(8, 0))

        status_row = ttk.Frame(conn, style="Panel.TFrame")
        status_row.pack(fill="x", padx=12, pady=(0, 12))

        self.status_dot = tk.Canvas(status_row, width=14, height=14, highlightthickness=0, bg=self.colors["panel"])
        self.status_dot.pack(side="left", pady=2)
        self._status_dot_id = self.status_dot.create_oval(2, 2, 12, 12, fill=self.colors["danger"], outline="")

        self.conn_label = ttk.Label(status_row, text="Not connected", background=self.colors["panel"])
        self.conn_label.pack(side="left", padx=(8, 0))

        # Live cards
        card = ttk.Frame(left, style="Card.TFrame")
        card.pack(fill="x")

        card_title = ttk.Label(card, text="Live Moisture Reading", style="CardTitle.TLabel")
        card_title.pack(anchor="w", padx=12, pady=(10, 4))

        self.moisture_value = ttk.Label(card, text="—", style="Value.TLabel")
        self.moisture_value.pack(anchor="w", padx=12, pady=(0, 12))

        status_card = ttk.Frame(left, style="Card.TFrame")
        status_card.pack(fill="x", pady=(14, 0))

        status_title = ttk.Label(status_card, text="System Status", style="CardTitle.TLabel")
        status_title.pack(anchor="w", padx=12, pady=(10, 8))

        row1 = ttk.Frame(status_card, style="Card.TFrame")
        row1.pack(fill="x", padx=12)

        ttk.Label(row1, text="Pump", background=self.colors["card"], foreground=self.colors["muted"]).pack(side="left")
        self.pump_value = ttk.Label(row1, text="—", style="BigStatus.TLabel", foreground=self.colors["text"])
        self.pump_value.pack(side="right")

        row2 = ttk.Frame(status_card, style="Card.TFrame")
        row2.pack(fill="x", padx=12, pady=(8, 0))

        ttk.Label(row2, text="Soil", background=self.colors["card"], foreground=self.colors["muted"]).pack(side="left")
        self.soil_value = ttk.Label(row2, text="—", style="BigStatus.TLabel", foreground=self.colors["text"])
        self.soil_value.pack(side="right")

        self.log_text = tk.Text(
            left,
            height=10,
            bg=self.colors["panel"],
            fg=self.colors["text"],
            insertbackground=self.colors["text"],
            relief="flat",
            wrap="word",
            font=("Consolas", 9),
        )
        self.log_text.pack(fill="both", expand=True, pady=(14, 0))
        self.log_text.configure(state="disabled")

        # Graph area
        graph_card = ttk.Frame(right, style="Card.TFrame")
        graph_card.pack(fill="both", expand=True)

        gtitle = ttk.Label(graph_card, text="Moisture Trend (Real-time)", style="CardTitle.TLabel")
        gtitle.pack(anchor="w", padx=12, pady=(10, 8))

        self.figure = Figure(figsize=(7.5, 4.5), dpi=100)
        self.figure.patch.set_facecolor(self.colors["card"])
        self.ax = self.figure.add_subplot(111)
        self.ax.set_facecolor(self.colors["card"])
        self.ax.grid(True, color=self.colors["grid"], linewidth=0.8, alpha=0.7)
        self.ax.tick_params(colors=self.colors["muted"])
        for spine in self.ax.spines.values():
            spine.set_color(self.colors["grid"])

        self.ax.set_xlabel("Time (s)", color=self.colors["muted"])
        self.ax.set_ylabel("Moisture Value", color=self.colors["muted"])

        (self.line,) = self.ax.plot([], [], color=self.colors["accent"], linewidth=2.4)

        self.canvas = FigureCanvasTkAgg(self.figure, master=graph_card)
        self.canvas_widget = self.canvas.get_tk_widget()
        self.canvas_widget.pack(fill="both", expand=True, padx=12, pady=(0, 12))

    # ---------- Config ----------
    def _load_config(self) -> None:
        if not CONFIG_PATH.exists():
            return
        try:
            data = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
        except Exception:
            return
        port = str(data.get("port") or "").strip()
        baud = str(data.get("baud") or "").strip()
        if port:
            self.port_var.set(port)
        if baud.isdigit():
            self.baud_var.set(baud)

    def _save_config(self) -> None:
        data = {
            "port": self.port_var.get().strip(),
            "baud": int(self._safe_int(self.baud_var.get().strip(), self.baud)),
        }
        try:
            CONFIG_PATH.write_text(json.dumps(data, indent=2), encoding="utf-8")
        except Exception:
            pass

    # ---------- Serial / Events ----------
    def _refresh_ports(self) -> None:
        ports = []
        try:
            ports = [p.device for p in list_ports.comports()]
        except Exception:
            ports = []

        current = self.port_var.get().strip()
        self.port_combo["values"] = ports
        if current and current in ports:
            self.port_var.set(current)
        elif ports and not current:
            self.port_var.set(ports[0])

        self._append_log(f"Ports: {', '.join(ports) if ports else '(none found)'}")

    def _on_connect(self) -> None:
        port = self.port_var.get().strip()
        if not port:
            self._append_log("Select a COM port (e.g., COM3).")
            return

        baud = self._safe_int(self.baud_var.get().strip(), self.baud)
        self.baud = baud
        self._save_config()

        self.serial_worker.start(port, baud, timeout=1.0)
        self._set_connection_state(True, detail=f"Connecting to {port}...")

    def _on_disconnect(self) -> None:
        self.serial_worker.stop()
        self._set_connection_state(False, detail="Disconnected")

    def _on_close(self) -> None:
        self.serial_worker.stop()
        self.root.destroy()

    def _poll_queues(self) -> None:
        # Logs
        while True:
            try:
                msg = self.log_queue.get_nowait()
            except queue.Empty:
                break
            self._append_log(msg)

        # Readings
        updated = False
        while True:
            try:
                reading = self.readings_queue.get_nowait()
            except queue.Empty:
                break

            if self.serial_worker.connected_port and not self._connected:
                self._set_connection_state(True, detail=f"Connected: {self.serial_worker.connected_port}")
            if self.serial_worker.connected_port is None and self._connected:
                self._set_connection_state(False, detail="Disconnected")

            if reading.moisture is not None:
                self._latest_moisture = reading.moisture
                updated = True
                # update series
                t = reading.ts - self._t0
                self._x.append(t)
                self._y.append(reading.moisture)
                if len(self._x) > self._series_capacity:
                    self._x = self._x[-self._series_capacity :]
                    self._y = self._y[-self._series_capacity :]

            if reading.soil:
                self._latest_soil = reading.soil
                updated = True
            if reading.pump:
                self._latest_pump = reading.pump
                updated = True

        if updated:
            self._render_live()

        self.root.after(100, self._poll_queues)

    def _schedule_graph_refresh(self) -> None:
        self._render_graph()
        self.root.after(self.interval_ms, self._schedule_graph_refresh)

    # ---------- Rendering ----------
    def _render_live(self) -> None:
        moisture_text = "—" if self._latest_moisture is None else str(self._latest_moisture)
        self.moisture_value.configure(text=moisture_text)

        pump = self._latest_pump
        soil = self._latest_soil

        pump_color = self.colors["muted"]
        if pump == "ON":
            pump_color = self.colors["warn"]
        elif pump == "OFF":
            pump_color = self.colors["accent"]

        soil_color = self.colors["muted"]
        if soil == "Dry":
            soil_color = self.colors["warn"]
        elif soil == "Wet":
            soil_color = self.colors["accent"]

        self.pump_value.configure(text=pump, foreground=pump_color)
        self.soil_value.configure(text=soil, foreground=soil_color)

    def _render_graph(self) -> None:
        if not self._x:
            # keep axes pretty even before data arrives
            self.ax.set_xlim(0, max(10, self.interval_ms / 1000.0 * 20))
            self.ax.set_ylim(0, 1023)
            self.line.set_data([], [])
            self.canvas.draw_idle()
            return

        x = self._x
        y = self._y
        self.line.set_data(x, y)

        xmin = max(0.0, x[-1] - (self.interval_ms / 1000.0) * 40.0)  # ~20s window at 500ms
        xmax = x[-1] + 0.5
        self.ax.set_xlim(xmin, xmax)

        ymin = max(0, min(y) - 40)
        ymax = min(1023, max(y) + 40)
        if ymax - ymin < 100:
            ymax = min(1023, ymin + 100)
        self.ax.set_ylim(ymin, ymax)

        self.canvas.draw_idle()

    def _set_connection_state(self, connected: bool, *, detail: str) -> None:
        self._connected = connected and (self.serial_worker.connected_port is not None or "Connecting" in detail)

        if connected and self.serial_worker.connected_port:
            dot = self.colors["accent"]
            text = detail
        elif connected:
            dot = self.colors["warn"]
            text = detail
        else:
            dot = self.colors["danger"]
            text = detail

        self.status_dot.itemconfigure(self._status_dot_id, fill=dot)
        self.conn_label.configure(text=text)

        if connected:
            self.connect_btn.configure(state="disabled")
            self.disconnect_btn.configure(state="normal")
        else:
            self.connect_btn.configure(state="normal")
            self.disconnect_btn.configure(state="disabled")

    def _append_log(self, msg: str) -> None:
        ts = time.strftime("%H:%M:%S")
        line = f"[{ts}] {msg}\n"
        self.log_text.configure(state="normal")
        self.log_text.insert("end", line)
        self.log_text.see("end")
        self.log_text.configure(state="disabled")

    @staticmethod
    def _safe_int(value: str, fallback: int) -> int:
        try:
            return int(value)
        except Exception:
            return fallback


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Smart Irrigation Dashboard (Tkinter)")
    parser.add_argument("--port", default="", help="Serial port (e.g., COM3). Overrides saved config.")
    parser.add_argument("--baud", type=int, default=9600, help="Baud rate (default: 9600)")
    parser.add_argument("--interval", type=int, default=500, help="Update interval ms (default: 500)")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    root = tk.Tk()
    DashboardApp(
        root,
        port=args.port.strip() or None,
        baud=args.baud,
        interval_ms=max(100, int(args.interval)),
    )
    root.mainloop()


if __name__ == "__main__":
    main()

