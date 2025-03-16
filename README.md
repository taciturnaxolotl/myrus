![scrapyard box with rainbow coming out and envoloping a camera](https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/logo.png)

# Myrus

![cad](https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/assembly.png)

<p align="center">
	<img src="https://raw.githubusercontent.com/taciturnaxolotl/carriage/main/.github/images/line-break-thin.svg" />
</p>

this is my team's project for `scrapyard columbus` we are making a gymbal that is controlled by a phone app that tracks faces; comunication is done over ~[`iroh`](https://www.iroh.computer/)~ `webserial` and the gymbal is controlled by the hackclub [`blot`](https://blot.hackclub.com) control board jerry rigged to an orpheus pico.

## pins

<img src="https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/acon-scrapyard-live-footage.png" width=320 align="right" />

| Pico Pin | Blot Pin | Description |
|----------|----------|-------------|
| `GP11` | `D10` | Step signal for Motor 1 |
| `GP10` | `D9` | Direction control for Motor 1 |
| `GP9` | `D2` | Enable/disable control for both motors |
| `GP8` | `D8` | Step signal for Motor 2 |
| `GP7` | `D7` | Direction control for Motor 2 |
| `GP9` | `D1` | Enable/disable control for both motors |
| `GND` | `GND` | Ground |
| `3V3` | `3V3` | Power |

## firmware

The firmware is written in circuitpython and is in the [`firmware`](/firmware) directory.
The firmware sends complete rotations based on serial input in two formats:
- `[1 or 2] [number]` -> rotate to a position in complete rotations
- `[1 or 2] zero` -> set current position as zero

The control is absolute - you specify the rotation count you want to go to rather than relative movement.

Important notes:
- Using 200 steps/rotation * 16 microsteps (very precise control)
- Motors must be enabled by setting enable pin LOW
- Movement speed controlled by delay between steps (currently 0.0001s) 
- Supports two motors controlled independently
- Coordinates maintained in number of steps from zero
- Direction controlled automatically based on target vs current position
- Movement is blocking - the pico will not respond to serial commands while moving

## web interface

The web interface is a pwa that is served from cloudflare pages. It uses the web serial api to communicate with the pico. The web interface is hosted at [myrus.dunkirk.sh](https://myrus.dunkirk.sh).

## cad

The cad was done in onshape around midnight of the `15th` and can be found and exported from here: [cad.onshape.com/documents/8d200c472...](https://cad.onshape.com/documents/8d200c472fc5b660efdf8352/w/ff1d53ebe00121ac7a3c9bc5/e/6edac687c4356b98c8934741?renderMode=0&uiState=67d649b588856c134638cb6b)

## schematics / blueprints

![blueprint](https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/blueprint.svg)

![blot schematic](https://raw.githubusercontent.com/taciturnaxolotl/myrus/master/.github/images/blot-schematic.svg)

<p align="center">
	<img src="https://raw.githubusercontent.com/taciturnaxolotl/carriage/main/.github/images/line-break.svg" />
</p>

<p align="center">
	<i><code>&copy 2025-present <a href="https://github.com/taciturnaxolotl">Kieran Klukas</a>, <a href="https://github.com/paytontech">Payton Curry</a>, and Elizabeth Klukas</code></i>
</p>

<p align="center">
	<a href="https://github.com/taciturnaxolotl/myrus/blob/master/LICENSE.md"><img src="https://img.shields.io/static/v1.svg?style=for-the-badge&label=License&message=AGPL 3.0&logoColor=d9e0ee&colorA=363a4f&colorB=b7bdf8"/></a>
</p>
