import board
import time
import digitalio
import supervisor

class MotorState:
    def __init__(self, step_pin, dir_pin, min_delay=0.001):
        self.step_pin = step_pin
        self.dir_pin = dir_pin
        self.position = 0
        self.min_delay = min_delay

    def step(self):
        self.step_pin.value = True
        time.sleep(self.min_delay)
        self.step_pin.value = False
        time.sleep(self.min_delay)

# Define pins for stepper motors
motor1_step = digitalio.DigitalInOut(board.GP11)
motor1_step.direction = digitalio.Direction.OUTPUT
motor1_dir = digitalio.DigitalInOut(board.GP10)
motor1_dir.direction = digitalio.Direction.OUTPUT
motor2_step = digitalio.DigitalInOut(board.GP8)
motor2_step.direction = digitalio.Direction.OUTPUT
motor2_dir = digitalio.DigitalInOut(board.GP7)
motor2_dir.direction = digitalio.Direction.OUTPUT
enable = digitalio.DigitalInOut(board.GP9)
enable.direction = digitalio.Direction.OUTPUT

# Enable motors
enable.value = False
print("Motors enabled")

STEPS_PER_ROT = 200 * 16 # Steps per rotation

motor1 = MotorState(motor1_step, motor1_dir, 0.0001)
motor2 = MotorState(motor2_step, motor2_dir, 0.0001)

while True:
    if supervisor.runtime.serial_bytes_available:
        command = input().strip().lower()
        parts = command.split()

        if len(parts) != 2:
            print("Invalid command. Use '[motor#] [position/zero]'")
            continue

        motor_num = int(parts[0])
        target = parts[1]

        if motor_num not in [1,2]:
            print("Invalid motor number. Use 1 or 2")
            continue

        motor = motor1 if motor_num == 1 else motor2

        try:
            if target == "zero":
                motor.position = 0
                print(f"Motor {motor_num} position zeroed")
                continue

            target_pos = float(target) * STEPS_PER_ROT
            steps = int(target_pos - motor.position)

            if steps > 0:
                motor.dir_pin.value = True
            else:
                motor.dir_pin.value = False
                steps = -steps

            print(f"Moving motor {motor_num} to position {target} rotations...")

            for _ in range(steps):
                motor.step()

            motor.position = target_pos
            print(f"Motor {motor_num} movement complete")

        except ValueError:
            print("Invalid position. Use number or 'zero'")
