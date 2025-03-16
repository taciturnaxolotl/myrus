import { SerialPort } from "serialport";
import express from "express";

const app = express();
app.use(express.json());

const serialPort = new SerialPort({ path: "/dev/ttyACM0", baudRate: 115200 });

app.post("/motor1", (req, res) => {
	const speed = req.body.speed;
	const command = `1 ${speed}\n`;
	serialPort.write(command);
	res.send("OK");
});

app.post("/motor2", (req, res) => {
	const speed = req.body.speed;
	const command = `2 ${speed}\n`;
	serialPort.write(command);
	res.send("OK");
});

app.all("*", (req, res) => {
	if (req.method !== "POST") {
		res.status(405).send("Method not allowed");
	} else {
		res.status(404).send("Not found");
	}
});

const port = 3000;
app.listen(port, () => {
	console.log(`Server running at http://localhost:${port}`);
});
