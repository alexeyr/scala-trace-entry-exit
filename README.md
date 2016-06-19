**Work in progress, current version is not usable!**

This is a library containing annotation macros to allow easily and
automatically log entering/leaving methods. Just import
`com.github.alexeyr.trace.entry_exit.TraceEntryExit` and annotate
your methods to insert logging statements in the beginning and the end of
the method, containing the method name, arguments and return value.
You can override default behavior by giving parameters to the annotation,
e.g. `@TraceEntryExit(logReturnValue = false)`.

You can also annotate classes/traits/objects to handle all
their methods (except for trivial `val`/`var` getters) at once,
using `@SkipTracing` on methods you want to skip and `@TraceEntryExit` to
override settings for a specific method.

[SLF4J](www.slf4j.org) and [Log4j2](http://logging.apache.org/log4j/2.x/)
are supported.
