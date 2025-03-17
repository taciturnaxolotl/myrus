import "./global.css";
import * as faceapi from "face-api.js";

let port: SerialPort;
let videoElement: HTMLVideoElement;
let lastError = 0;
let integral = 0;
let isDragging = false;
let joystickX = 0;
let joystickY = 0;

// PID constants
const Kp = 0.5;
const Ki = 0.1;
const Kd = 0.2;

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
												<button id="startTracking">Start Face Tracking</button>
												<video id="webcam" width="640" height="480" autoplay muted></video>
												<canvas id="overlay" style="position: absolute;"></canvas>

												<div id="joystick" style="width: 200px; height: 200px; border: 2px solid black; border-radius: 50%; position: relative; margin: 20px;">
																<div id="joystickHandle" style="width: 20px; height: 20px; background: red; border-radius: 50%; position: absolute; top: 90px; left: 90px; cursor: pointer;"></div>
												</div>
												<div>
																<span>Motor Values - X: </span><span id="motor1Value">0</span>
																<span>, Y: </span><span id="motor2Value">0</span>
																<button id="sendJoystick">Send</button>
												</div>
								</div>
								<div style="width: 300px; padding: 20px; border-left: 1px solid #ccc; overflow-y: auto;">
												<h3>Serial Log</h3>
												<pre id="serialLog" style="white-space: pre-wrap; margin: 0;"></pre>
								</div>
				</div>
`;

async function loadFaceDetectionModels() {
	await faceapi.nets.tinyFaceDetector.loadFromUri("/models");
	await faceapi.nets.faceLandmark68Net.loadFromUri("/models");
}

async function startWebcam() {
	try {
		const stream = await navigator.mediaDevices.getUserMedia({ video: true });
		videoElement.srcObject = stream;
	} catch (err) {
		console.error("Error accessing webcam:", err);
		alert("Failed to access webcam");
	}
}

function calculatePID(error: number) {
	integral += error;
	const derivative = error - lastError;
	lastError = error;

	return Kp * error + Ki * integral + Kd * derivative;
}

async function trackFaces() {
	const canvas = document.getElementById("overlay") as HTMLCanvasElement;
	canvas.width = videoElement.width;
	canvas.height = videoElement.height;
	const displaySize = {
		width: videoElement.width,
		height: videoElement.height,
	};

	setInterval(async () => {
		const detections = await faceapi.detectAllFaces(
			videoElement,
			new faceapi.TinyFaceDetectorOptions(),
		);

		if (detections.length > 0) {
			const face = detections[0];
			const centerX = face.box.x + face.box.width / 2;
			const targetX = videoElement.width / 2;
			const error = (centerX - targetX) / videoElement.width;

			const adjustment = calculatePID(error);
			await sendMotorCommand(1, adjustment);
			await sendMotorCommand(2, -adjustment);

			// Draw face detection
			const context = canvas.getContext("2d");
			if (context) {
				context.clearRect(0, 0, canvas.width, canvas.height);
				faceapi.draw.drawDetections(canvas, detections);
			}
		}
	}, 100);
}

async function connectSerial() {
	try {
		port = await navigator.serial.requestPort();
		await port.open({ baudRate: 115200 });

		if (port.writable == null) {
			throw new Error("Failed to open serial port - port is not writable");
		}

		console.log("Connected to serial port");
		appendToLog("Connected to serial port");

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

async function sendMotorCommand(motorNum: number, rotation: number) {
	if (!port) {
		alert("Please connect serial port first");
		return;
	}

	if (!port.writable) {
		alert("Serial port is not writable");
		return;
	}

	const writer = port.writable.getWriter();
	const encoder = new TextEncoder();
	const data = `${motorNum} ${rotation}\r`;

	try {
		await writer.write(encoder.encode(data));
		appendToLog(`Sent to motor ${motorNum}: ${rotation} rotations`);
	} catch (err) {
		console.error("Write error:", err);
		appendToLog(`Error sending to motor ${motorNum}: ${err}`);
	} finally {
		writer.releaseLock();
	}
}

function updateJoystickPosition(x: number, y: number) {
	const joystick = document.getElementById("joystick");
	const handle = document.getElementById("joystickHandle");
	if (!joystick || !handle) return;

	const bounds = joystick.getBoundingClientRect();
	const radius = bounds.width / 2;

	// Calculate relative position from center
	const relX = x - bounds.left - radius;
	const relY = y - bounds.top - radius;

	// Calculate distance from center
	const distance = Math.sqrt(relX * relX + relY * relY);

	// Normalize to radius if outside circle
	const normalizedX = distance > radius ? (relX / distance) * radius : relX;
	const normalizedY = distance > radius ? (relY / distance) * radius : relY;

	// Update handle position
	handle.style.left = normalizedX + radius - 10 + "px";
	handle.style.top = normalizedY + radius - 10 + "px";

	// Update values (-0.5 to 0.5 range)
	joystickX = normalizedX / (radius * 2);
	joystickY = normalizedY / (radius * 2);

	document.getElementById("motor1Value")!.textContent = joystickX.toFixed(2);
	document.getElementById("motor2Value")!.textContent = joystickY.toFixed(2);
}

function defaultPageRender() {
	const app = document.querySelector<HTMLDivElement>("#app");
	if (!app) throw new Error("App element not found");
	app.innerHTML = createTemplate();

	videoElement = document.getElementById("webcam") as HTMLVideoElement;

	const joystick = document.getElementById("joystick");
	const handle = document.getElementById("joystickHandle");

	if (joystick && handle) {
		handle.addEventListener("mousedown", () => {
			isDragging = true;
		});

		document.addEventListener("mousemove", (e) => {
			if (isDragging) {
				updateJoystickPosition(e.clientX, e.clientY);
			}
		});

		document.addEventListener("mouseup", () => {
			if (isDragging) {
				isDragging = false;
			}
		});
	}

	document.getElementById("connect")?.addEventListener("click", connectSerial);
	document
		.getElementById("startTracking")
		?.addEventListener("click", async () => {
			await loadFaceDetectionModels();
			await startWebcam();
			trackFaces();
		});
	document.getElementById("sendJoystick")?.addEventListener("click", () => {
		sendMotorCommand(1, joystickX);
		sendMotorCommand(2, joystickY);
	});

	document.addEventListener("keydown", (e) => {
		if (e.key === "Enter") {
			sendMotorCommand(1, joystickX);
			sendMotorCommand(2, joystickY);
		}
	});
}

function handleRoute() {
	defaultPageRender();
}

handleRoute();
