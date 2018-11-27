# Curve fitting

A simple application demonstrating some of the capabilities of the [Metaprob probabilistic programming language](https://github.com/probcomp/metaprob).

## Prerequisites

* The instructions below assume that you have the [Clojure command-line tools](https://clojure.org/guides/deps_and_cli) installed.
* This application, [like Quil](https://github.com/quil/quil/issues/228), is not compatible with Java 9 or higher.

## Running

Launch a Clojure REPL:

    make dev

Start the server by evaluating `(go)` at the REPL.

Once you have made changes you can reload the visualization with `(reset)`.

You can close the visualization with`(halt)`.

## Usage

Once you are running the application you can place points by clicking anywhere in the window. The application will then sample polynomials from the prior and display them, shading each according to how likely it explains the clicked points.

### Keyboard shortcuts

- `c` will clear the screen.
- Any other key will refresh the curves.
- Digits followed by `return` will set the maximum number of curves.
