import "./global.css";

let port: SerialPort;

// Check if Serial API is supported
if (!("serial" in navigator)) {
	alert(
		"Web Serial API is not supported in this browser. Please use Chrome or Edge.",
	);
}

const createTemplate = () => `
				<div style="display: flex; flex-direction: row; height: 100vh;">
								<div style="display: flex; flex-direction: column; align-items: center; justify-content: center; flex: 1; gap: 20px;">
												<button id="connect">Connect Serial Port</button>

												<div>
																<label>Left Motor (1) Rotations:</label>
																<input type="range" id="motor1" min="-10" max="10" step="0.1" value="0">
																<span id="motor1Value">0</span>
																<button id="send1">Send</button>
												</div>

												<div>
																<label>Right Motor (2) Rotations:</label>
																<input type="range" id="motor2" min="-10" max="10" step="0.1" value="0">
																<span id="motor2Value">0</span>
																<button id="send2">Send</button>
												</div>
								</div>
								<div style="width: 300px; padding: 20px; border-left: 1px solid #ccc; overflow-y: auto;">
												<h3>Serial Log</h3>
												<pre id="serialLog" style="white-space: pre-wrap; margin: 0;"></pre>
								</div>
				</div>
`;

async function connectSerial() {
	try {
		port = await navigator.serial.requestPort();
		await port.open({ baudRate: 115200 });

		// Verify port is open before continuing
		if (port.writable == null) {
			throw new Error("Failed to open serial port - port is not writable");
		}

		console.log("Connected to serial port");
		appendToLog("Connected to serial port");

		// Start reading from the serial port
		while (port.readable) {
			const reader = port.readable.getReader();
			try {
				while (true) {
					const { value, done } = await reader.read();
					if (done) break;
					const decoded = new TextDecoder().decode(value);
					appendToLog(decoded);
				}
			} catch (error) {
				console.error(error);
			} finally {
				reader.releaseLock();
			}
		}
	} catch (err) {
		console.error("Serial port error:", err);
		alert(
			"Failed to open serial port. Please check your connection and permissions.",
		);
	}
}

function appendToLog(message: string) {
	const log = document.getElementById("serialLog");
	if (log) {
		log.textContent += message + "\n";
		log.scrollTop = log.scrollHeight;
	}
}

async function sendMotorCommand(motorNum: number) {
	if (!port) {
		alert("Please connect serial port first");
		return;
	}

	if (!port.writable) {
		alert("Serial port is not writable");
		return;
	}

	const rotations = (
		document.getElementById(`motor${motorNum}`) as HTMLInputElement
	).value;
	const writer = port.writable.getWriter();
	const encoder = new TextEncoder();
	const data = `${motorNum} ${rotations}\r`;

	try {
		await writer.write(encoder.encode(data));
		appendToLog(`Sent to motor ${motorNum}: ${rotations} rotations`);
	} catch (err) {
		console.error("Write error:", err);
		appendToLog(`Error sending to motor ${motorNum}: ${err}`);
	} finally {
		writer.releaseLock();
	}
}

function defaultPageRender() {
	const app = document.querySelector<HTMLDivElement>("#app");
	if (!app) throw new Error("App element not found");
	app.innerHTML = createTemplate();

	// Add slider value display updates
	document.getElementById("motor1")?.addEventListener("input", (e) => {
		const value = (e.target as HTMLInputElement).value;
		const display = document.getElementById("motor1Value");
		if (display) display.textContent = value;
	});

	document.getElementById("motor2")?.addEventListener("input", (e) => {
		const value = (e.target as HTMLInputElement).value;
		const display = document.getElementById("motor2Value");
		if (display) display.textContent = value;
	});

	document.getElementById("connect")?.addEventListener("click", connectSerial);
	document
		.getElementById("send1")
		?.addEventListener("click", () => sendMotorCommand(1));
	document
		.getElementById("send2")
		?.addEventListener("click", () => sendMotorCommand(2));
}

function handleRoute() {
	defaultPageRender();
}

handleRoute();
