## curvefitting

Plot a number of exponential equations on a 2D plane. Let them fade out over time.

### run

This example has two halves: the server (which creates equations to plot and sends them to the client), and the client (which plots the equations in a browser).

#### server

Launch a Clojure REPL and start automatic ClojureScript recompilation and hot reloading with:

    make server

Start the server by evaluating `(go)` at the REPL.

Once you have made changes you can update the running process with `(reset)`.

You can stop sending equations to the client (but maintain the websocket connection) by "suspending" the system with `(suspend)`. Resume with `(resume)`.

You can completely shutdown the server by evaluating `(halt)`. This will disconnect any clients.

#### observe

Visit `http://localhost:3333` in your browser. You should see plots starting to appear in the window. Click the `Animate` to start the "fade out" animation. Remember you can suspend the server to pause the stream of equations.
