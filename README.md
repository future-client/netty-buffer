#netty-buffer

https://netty.io/wiki/using-as-a-generic-library.html - Don't use this over the upstream Netty module, all tests pass, but it will not be up-to-date, it has features removed, the API stability was broken, it has worse performance, it has worse memory allocation and thread safety may have been broken in some situations.

This library will probably not be fixed outside our use case of shimming existing usage of Netty in other projects for compatibility reasons.

The only advantage of this extracted version is that it's a pure Java library, no longer uses reflection, no longer uses internal APIs such as `sun.misc.Unsafe` even if available, and it's not a dependency of any other project or other netty modules.

Extracted from the Netty project (commit hash: f235b37c281977b5428dacb32cc44072c9a441e5, version 4.1.76.Final-SNAPSHOT).