import { SerialPort } from "serialport";

const server = Bun.serve({
	port: 3000,
	async fetch(req) {
		const url = new URL(req.url);

		if (req.method !== "POST") {
			return new Response("Method not allowed", { status: 405 });
		}

		const body = await req.json();
		const speed = body.speed;

		if (url.pathname === "/motor1") {
			const command = `1 ${speed}\n`;
			serialPort.write(command);
			return new Response("OK");
		}

		if (url.pathname === "/motor2") {
			const command = `2 ${speed}\n`;
			serialPort.write(command);
			return new Response("OK");
		}

		return new Response("Not found", { status: 404 });
	},
});

const serialPort = new SerialPort({ path: "/dev/ttyACM0", baudRate: 115200 });

console.log(`Server running at http://localhost:${server.port}`);
