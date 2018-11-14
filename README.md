## curvefitting

Plot a number of exponential equations on a 2D plane. Let them fade out over time.

### run

This example has two halves: the server (which creates equations to plot and sends them to the client), and the client (which plots the equations in a browser).

#### server

Start the server process with

    make server

Connect to the nREPL at the port indicated in the console. Open `equations/server.clj` and eval the buffer. Start the server loop by evaluating this form in the comment block at the bottom of the file:

    (def system
        (ig/init config))

You can stop sending equations to the client (but maintain the websocket connection) by "suspending" the system: evaluate `(ig/suspend! system)`. Resume by evaluating:

    (def system
        (ig/resume config system))

You can completely shutdown the server by evaluating `(ig/halt! system)`. This will disconnect any clients.

#### client

In another shell, compile the client and start the figwheel server (which will push code changes to your running client):

    make figwheel

#### observe

Visit `http://localhost:3333` in your browser. You should see plots starting to appear in the window. Click the `Animate` to start the "fade out" animation. Remember you can suspend the server to pause the stream of equations.
