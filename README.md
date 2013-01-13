A Monte Carlo tree search agent for Ms Pac-Man
===================================

[Ms Pac-Man](http://en.wikipedia.org/wiki/Ms._Pac-Man) is a game released by
Midway Manufacturing in 1982.  It is based on the original Pac-Man game and
adds new maps and semi-random ghost behaviour.

The [Ms Pac-Man vs Ghosts League](http://www.pacman-vs-ghosts.net/) is an AI
competition based on the game.  The organisers have released a framework which
emulates Ms Pac-Man and allows users to write agents in Java.

[Monte Carlo tree search](http://www.mcts.ai/?q=mcts) is a decision making
process which uses a heuristically-guided sampling method to cut down the
search space.

This project is a game-playing agent for the Ms Pac-Man vs Ghosts League
framework, using Monte Carlo tree search as its main decision-making process.
It was written by Stewart MacKenzie-Leigh as part of a summer research
placement in the [Computer and Information Sciences department of the
University of Strathclyde](http://www.strath.ac.uk/cis/).  The code is based
loosely on initial work by Matthias Lenz, and the research was carried out
with Dr John Levine.  We wrote a paper for
[PlanSIG 2012](http://www.scm.tees.ac.uk/users/p.gregory/plansig2012/),
also available [here](http://www.stewartml.co.uk/wp-content/uploads/2013/01/mackenzie-leigh_et_al.pdf).

If you want to read the source code to see what it does,
src/pacman/entries/pacman/MyPacMan.java is a good place to start.

The agent source code is released under the MIT License; you can find a copy
in the LICENSE file.  Please note that the framework is included as the
PacManVsGhosts6.2.jar file, and has a separate license available inside the
jar in copyright.txt.  The data/ directory is also part of the framework and
shares the same license.

You can do what you like with the code - feel free to drop me a line to tell
me what you're doing with it.