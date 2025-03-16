<h1 align="center">
    <img src="https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/logo.png" alt="scrapyard box with rainbow coming out and envoloping a camera"/><br/>
    <span>Myrus</span>
</h1>

<p align="center">
    <i>A project for scrapyard</i>
</p>

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

![blot schematic](https://raw.githubusercontent.com/taciturnaxolotl/myrus/master/.github/images/blot-schematic.svg)

## web interface

![web interface](https://raw.githubusercontent.com/taciturnaxolotl/myrus/main/.github/images/web-interface.png)

The web interface is a pwa that is served from cloudflare pages. It uses the web serial api to communicate with the pico. The web interface is hosted at [myrus.dunkirk.sh](https://myrus.dunkirk.sh).

<p align="center">
	<img src="https://raw.githubusercontent.com/taciturnaxolotl/carriage/main/.github/images/line-break.svg" />
</p>

<p align="center">
	<i><code>&copy 2025-present <a href="https://github.com/taciturnaxolotl">Kieran Klukas</a>, <a href="https://github.com/paytontech">Payton Curry</a>, and Elizabeth Klukas</code></i>
</p>

<p align="center">
	<a href="https://github.com/taciturnaxolotl/myrus/blob/master/LICENSE.md"><img src="https://img.shields.io/static/v1.svg?style=for-the-badge&label=License&message=AGPL 3.0&logoColor=d9e0ee&colorA=363a4f&colorB=b7bdf8"/></a>
</p>
