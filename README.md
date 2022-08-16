SEEDE: Simulataneous Editing and Execution Development Environment

SEEDE is a tool for live programming for real Java programs.  It assumes the user is using
the debugger and has stopped at the beginning of a routine.  It then simulates execution of
that routine (and everything it calls), updating the result on each keystroke.	The result
includes graphics and I/O done correctly for live programming.

SEEDE requires the seedebb package (which includes the code bubbles user interface for
SEEDE).  It should be downloaded along with this repository before either is built.
Additionally, ivy and code bubbles should be downloaded and compiled before attempting
to build SEEDE.  All of these should be in the same directory, i.e. ../ivy, ../bubbles,
and ../seedebb should refer to the cloned repositories for ivy, code bubbles, and
seedebb respectivley.

Building SEEDE will also build seedebb and will install the result as a plug in in the
code bubbles environment. Once all the packages are installed (and ivy and code bubbles
have been built successfully), just run ant at the top level of seede.	Note that ant
can also be run in any of the source subdirectories to just compile that package.

