## Syntax ##
Image processing can use assignments, expressions, if statements, and loops.

### Assignments ###
Assignment statements are of the form "[variable](variable.md) = [expression](expression.md)". Variables are not declared before use; just assign a variable to create a new one.

### Expressions ###
Expressions can be combinations of numbers, operations, and function calls. All expressions are of integer type; floating point and boolean values are not currently supported. The available operations are:
```
+ : addition
- : subtraction
* : multiplication
/ : integer division
% : mod
& : bitwise and
| : bitwise or
^ : bitwise xor
>> : right shift
<< : left shift
```

As usual, multiplication and division have precedence over other operations, and you can use parentheses to specify the evaluation order. Examples of assignments and expressions:
```
sum = 0

half = width / 2

diff = abs(y - brightness(row, col+1))
```

### Conditionals ###
You can conditionally execute code by using if statements. Examples:
```
if (brightness>limit) numbright = numbright + 1

if total<0 {
  total = 0
  negative = 1
}
```
As the examples show, an if statement compares a variable to another variable or number. If the comparison is true, the following statement is executed. A block of multiple statements can be executed by putting braces around them. Parentheses around the comparison are optional. Indenting lines inside the block is not required, but recommended for clarity.

If statements have the following limitations, which may be relaxed in future versions:
  * You can only compare single values, not expressions. "if x>y+10" is invalid. If you need to do that you could set a separate variable to y+10, and compare it against x.
  * When using braces to conditionally execute a block, the opening brace must be on the same line as the if, and the closing brace must be on a line by itself.

### While Loops ###
A while loop repeatedly executes its code until its condition is true. The syntax is the same as the if statement, except that "if" is replaced by "while". This code will add random numbers to a variable until the total is greater than 100:
```
total = 0
while total<100 {
  total = total + random(10)
}
```

While loops have similar restrictions to if statements: the compared values must be single variables or numbers, and the braces must be placed as in the example.

### For Loops ###
For loops are a special case of while loops, intended to make it easy to have a variable count from a lower to an upper value. If you wanted to add the numbers from 0 to 9, you could use this while loop:
```
total = 0
i = 0
while i<10 {
  total = total+i
  i = i+1
}
```
This for loop does the same thing:
```
total = 0
for (i,10) {
  total = total+i
}
```
The for loop sets the value of i to 0 and executes its statements repeatedly, adding 1 to i after each iteration. When i is equal to the limit, in this case 10, the loop will exit. Note that this means that the loop statements are not executed with i equal to the limit. If you wanted to include 10 in the sum then you would need to use "for i,11". As with "if" and "while", parentheses are optional and the values must be numbers or individual variables.

### Return ###
As discussed in Script Modes, your code may end with a return statement. If it does, the return value will specify the color of the output pixel. Normally the return value will be the result of calling the gray or rgb functions to return a grayscale or color pixel, for example:
```
return gray(2*edge)
return rgb(b, g, r)
```

## Script Modes ##
There are two ways that Image Playground will run your script. If your script contains a "return" statement, your code will run once for every pixel in the input image, and the value that you return will be the corresponding pixel in the output image. In this mode, your script will have access to the following variables:
```
y : brightness of the input pixel, from 0 to 255
r : red component of the input pixel, from 0 to 255
g : green component of the input pixel, from 0 to 255
b : blue component of the input pixel, from 0 to 255
row : row number of the input pixel, starting from the top at 0
col : column number of the input pixel, starting from the left at 0
width : width of the input image in pixels
height : height of the input image in pixels
```

If your script does **not** contain a return statement, it will be called only once for each input image. Your script is then responsible for gathering whatever information it needs from the input image, usually by looping over the rows and columns, and must also set the individual pixels in the output image. This mode requires writing more code, but allows more flexibility. For example, you can make multiple passes over the input image, or change previous output pixels based on the values of pixels you see later. In this mode, the only variables directly available to your code are "width" and "height". You can get pixel data from the input image by calling the functions listed below.

This one-line script produces a grayscale negative of the input image:
```
return gray(255-y)
```

The equivalent script without a return statement, which manually loops over the pixels in the input image and gets brightness values:
```
for (row,height) for (col,width) {
  y = getbright(row,col)
  setgray(row,col,255-y)
}
```

## API ##
There are several functions available for your scripts to call.

### Math Functions ###
```
abs(x) : returns the absolute value of x
min(x, y) : returns the smaller of x and y
max(x, y) : returns the greater of x and y
clamp(x, a, b) : if x is less than a, returns a. If x is greater than b, returns b. Otherwise returns x itself. Equivalent to min(b, max(x, a))
ifeq(x, y, a, b) : if x is equal to y, returns a, otherwise returns b
ifgt(x, y, a, b) : if x is greater than y, returns a, otherwise returns b
random(x) : returns a random integer between 0 and x-1
```

### Pixel Reading Functions ###
```
getbright(row, col): returns the brightness (between 0 and 255) of the pixel in the input image at the given row and column
getred(row, col): returns the red component (0-255) of the pixel in the input image at the given row and column
getgreen(row, col): returns the green component (0-255) of the pixel in the input image at the given row and column
getblue(row, col): returns the blue component (0-255) of the pixel in the input image at the given row and column
getcolor(row, col): returns a value for the color of the pixel in the input image at the given row and column
```

### Pixel Writing Functions ###
These functions set the values of output pixels. Scripts that do not use return statements will use these functions to create the output image.
```
setrgb(row, col, red, green, blue): sets the output pixel at the given row and column to have the given red, green, and blue components, where 0 is the minimum and 255 is the maximum
setgray(row, col, brightness): sets the output pixel at the given row and column to the grayscale value with the given brightness, where 0 is the minimum and 255 is the maximum
setcolor(row, col, color): sets the output pixel at the given row and column to the given color value, which normally will be the result of a getcolor function call
```

### Color Value Functions ###
These functions are used by the return statement and pixel writing functions to specify to the value of the output pixel. They automatically clamp their parameters to between 0 and 255; that is, calling gray(400) has the same result as gray(255), which is a white pixel.
```
gray(y) : returns value for a grayscale pixel with the given brightness, where 0 is black and 255 is white
rgb(r, g, b) : returns value for a color pixel with the given red, green, and blue components.
```