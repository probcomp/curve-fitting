## equations

Plot a number of exponential equations on a 2D plane. Let them fade out over time.

### run

This example has two halves: the server (which creates equations to plot and sends them to the client), and the client (which plots the equations in a browser).

Start the server process with

    make server

Connect to the nREPL at the port indicated in the console. Open `equations/server.clj` and eval the buffer. Start the server loop by evaluating the the `(start!)` form in the comment at the bottom of the file.

Now we'll compile and open the client:

    make figwheel

Then visit `http://localhost:3333` with your browser. You should see plots starting to appear in the window. Click the `Animate` to start the "fade out" animation.
