import board
import time
import digitalio

# Define pins for stepper motors
motor1_step = digitalio.DigitalInOut(board.GP10)
motor1_step.direction = digitalio.Direction.OUTPUT
motor1_dir = digitalio.DigitalInOut(board.GP9)
motor1_dir.direction = digitalio.Direction.OUTPUT
motor2_step = digitalio.DigitalInOut(board.GP8)
motor2_step.direction = digitalio.Direction.OUTPUT
motor2_dir = digitalio.DigitalInOut(board.GP7)
motor2_dir.direction = digitalio.Direction.OUTPUT
enable = digitalio.DigitalInOut(board.GP1)
enable.direction = digitalio.Direction.OUTPUT

# Enable motors
enable.value = False
print("Motors enabled")

# Function to step motor
def step(step_pin):
    step_pin.value = True
    time.sleep(0.000001)  # 1 microsecond
    step_pin.value = False
    time.sleep(0.000001)  # 1 microsecond

while True:
    # Set direction forward
    motor1_dir.value = True
    motor2_dir.value = True
    print("Moving motors forward...")

    # Run motors forward for 1 second
    start = time.time()
    while time.time() - start < 1:
        step(motor1_step)
        step(motor2_step)
        time.sleep(0.001)

    print("Forward movement complete")

    # Set direction reverse
    motor1_dir.value = False
    motor2_dir.value = False
    print("Moving motors in reverse...")

    # Run motors reverse for 1 second
    start = time.time()
    while time.time() - start < 1:
        step(motor1_step)
        step(motor2_step)
        time.sleep(0.001)

    print("Reverse movement complete")
