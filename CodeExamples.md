## Negative ##
A grayscale negative of the input image:
```
return gray(255-y)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/negative_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/negative_half.png)

## Emboss ##
```
d = 2*(y-getbright(row-1, col-1))
return gray(d + 128)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/emboss-boojie_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/emboss-boojie_half.png)

## Edge Detection ##
```
e = abs(getbright(row-1, col) + getbright(row+1, col) - 2*y) + abs(getbright(row, col-1) + getbright(row, col+1) - 2*y)
return rgb(0, 4*e, 0)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/edge-boojie2_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/edge-boojie2_half.png)

## Static ##
This produces mostly random static, but with enough unmodified pixels that you can still make out the original image. Play with the random parameters for different results.
```
return gray(ifgt(random(3), 0, random(255), y)
```

## Color Cycle ##
Turns green to red, blue to green, and red to blue.
```
return rgb(g, b, r)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/colorcycle_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/colorcycle_half.png)

## Intensify ##
Makes bright colors brighter and dark colors darker.
```
rr = 2*r - 256
gg = 2*g - 256
bb = 2*b - 256
return rgb(rr, gg, bb)
```
![http://wiki.image-playground.googlecode.com/git/screenshots/intensify_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/intensify_half.png)

## Picture in Picture ##
This is a more complex script that does not use a "return" statement, instead handling its own pixel reading and writing. It loops over the input image multiple times to draw several successively smaller copies to the output image.
```
ratio = 1
while ratio<=16 {
  maxw = width/ratio
  maxh = height/ratio
  for (row,maxh) for (col,maxw) {
    x = getcolor(row*ratio, col*ratio)
    setcolor(row, width-maxw+col, x)
  }
  ratio = 2*ratio
}
```
![http://wiki.image-playground.googlecode.com/git/screenshots/infinite-cat_half.png](http://wiki.image-playground.googlecode.com/git/screenshots/infinite-cat_half.png)

[Let me know](mailto:bnenning@google.com) if you come up with your own cool effects!