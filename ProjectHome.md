An Android app that allows you to enter image processing code, and applies it in real time to the image from the device's camera. The code you enter uses a simple syntax that resembles Javascript, and is compiled directly to the Dalvik VM using [dexmaker](http://code.google.com/p/dexmaker/) for maximum performance.

## Example ##
This code performs an emboss effect.
```
d = 2 * (y-getbright(row-1, col-1))
return gray(d + 128)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/emboss-library_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/emboss-library_half.png)

This is a more complex example using loops to manually generate the output:
![http://wiki.image-playground.googlecode.com/git/screenshots/infinite-cat_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/infinite-cat_half.png)

See CodeSyntax for details of writing code, and CodeExamples for more example effects.

## Download ##
The app isn't yet available on Google Play, but you can download and install the current development version from the [downloads page](http://code.google.com/p/image-playground/downloads/list).