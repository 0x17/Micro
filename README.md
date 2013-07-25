# Micro
Very minimalistic Scala game framework built on top of libgdx.

All assets must be placed in "data/" sub folder by convention.
* Images should be ".png"-files.
* Sounds should be ".wav"-files.
* Music should be ".mp3"-files.
* Fonts should be ".ttf"-files.

## Globals
scrW - screen width

scrH - screen height

## Methods

### Control flow
init(caption, scrSize, initCallback, drawCallback) - start application.
* caption - window title
* scrSize - screen size to use given as pair (w,h).
* initCallback - called once after everything was setup.
* drawCallback - callback is called each frame.

quit - close application

### Input
keyPressed(keyCode) => bool - true, iff. key w/ given code is pressed.

mouseState => MouseState(pos,lmb,mmb,rmb) - state of mouse (lmb=left mouse button, ...)

### Visuals
drawImage(name, x, y, rz, sx, sy) - draw image transformed at coordinates. Origin is bottom left.
* name - name of .png file (w/out extension) in "data/" path.
* rz - rotation around z-axis in degrees CCW.
* sx - scaling factor along x-axis.
* sy - scaling factor along y-axis.

getImageDim(name) - dimensions of image (w,h) in pixels

#### Font
setFont(name, size) - set font for subsequent drawText calls.
* name - name of .ttf file (w/out extension) in "data/" path.
* size - size of font for subsequent drawText calls.

drawText(text, x, y) - draw text w/ currently selected font. Origin bottom left.
* text - string of text to be drawn.

### Audio
playSound(name)

playSong(name, looping)

stopSong(name)
