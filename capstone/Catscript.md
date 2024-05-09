# Catscript Guide

This document should be used to create a guide for catscript, to satisfy capstone requirement 4

## Introduction

Catscript is a simple scripting langauge.  Here is an example:

```
var x = "foo"
print(x)
```

# Features

### Expressions

#### Equality Expression 
Evaluates comparison expressions, compares them equal each other. Uses equal or not equal using symbols  “==” and “!=”.
```
name != false;      name = true;
```
#### Comparison Expression
Evaluates two mathematical expressions and declares them as greater, less, greater than or equal to, or less than or equal to each other using“>,<, >=, <=” symbols.
```
2 > 1;       val >= 1;          2 = 2;
```
#### Additive Expression
Evaluates the addition or subtraction between two expressions using “-,+”. Can be used in conjunction, but order does matter, using parentheses symbols as precedence
```
2 + 1;        1 + ( 2 + 5)        (2 - 1) + (5 - 1)
```
#### Factor Expression
Evaluates the multiplication or division  between two expressions using “*, /”. These symbols take precedence over additive expressions unless parentheses symbols are involved
```
1 * 2;         1 * 2 + 3;       1 * ( 3 + 5);
```
#### Unary Expression
Not the same as a comparison or equality expression. This function evaluates whether an expression has a “! , not” label that is attached to it.
```
not false;        !false;
```
#### Function Call Expression
Evaluates if the expression starts with a call or identifier and is trailed by arguments within parentheses.
```
Add(5, 6, 0);        Pop(value);
```





### Statements

#### For Statement 
Operates like any other mainstream programming language(Java, Python, C) for loop. Includes an identifier variable, its incrementation, and its arguments expression with a statement within its block. The identifier represents the count of the loops iteration or how many times its inside expression is executed. The incrementation variable determines down the code increments, whether it moves down, up, when it stops, or at what rate. For loops can be nested in one another as well.
```
for(i: list)                     
{ remove(i); }
```
```
for(i in firstLoop)
{
for(a in namesList)
{
print(names);
}
}
```

#### If Statement
Like For Statement, operates like a Java if statement that executes the function upon the conditions provided are met. The” if” is followed by parenthesis with a condition statement inside, which determines whether the program continues to whatever is inside the block of code. If met, the statement or expression inside is executed.  You can stack If statements in conjunction, or even nest them inside on another alongside else statements.
```
if (value == 1)
{
return true;
} 
else
{ return false; }
```

#### Print Statement

Is responsible for displaying expressions onto the terminal for the user to see. Any text within the print statement will be outputted, but variables that are included in the print statement might not. They must have some type of text returned when called, or no output will be given. In some cases, indexes, or spaces in memory might be printed instead of a desired text. These variables must have some text associated to them beforehand, since Catscript does not understand what needs to be printed.
```
print("hello world ");              output:   hello world
```
```
val = “one”        print(val);          output:   one
```

#### Assignment Statement
Assigns a value to a variable, but only after that variable has been created. You cannot create a variable and give it a value in one line of code. One can modify or override previous assignments to protect it. Catscript also prevents reassigning its value if the type is different, so an int variable cannot be given a bool later on. This applies when the variable has not had a value assigned to it and afterwards.
```
Name = “John”;
```
```
cannot do: var Name = new var “John”
```


#### Function Call Statement
Executes an expression function call. The function called would already have expressions, conditions, etc. built beforehand. Like assignment statement, instantiating and assigning the functions content cant happen in one step. When calling this function, the parameters it includes are required to me included as well.
```
print(Name, name2); 
```
```
cannot do: print(Name); (if the function requires both parameters)
```


#### Return Statement
Can be executed to send a program back to its original caller or is part of a body of code. This is done when a program is completed or used to break it when conditions fail. Also is used to return a value back to the place that called upon it. They can return any type of value, as long as the function type matches the type of value being returned.
```
print(“hello”)
Return;
```
```
val = 1;
return val;
```


#### Function Definition Statement
Creates a new function. Must include an identifier name and a block of code within it with some type of executable code or expression. This is required as the call to this function must return something. You can also include parameters after its identifier to take in variables to be performed on or evaluate them in a condition statement.
```
String whatsUp(name)
{
Return name + “, how are you doing today?”
}
```