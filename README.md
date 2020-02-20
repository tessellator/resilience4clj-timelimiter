# resilience4clj-timelimiter

A small Clojure wrapper around the
[resilience4j TimeLimiter module](https://resilience4j.readme.io/docs/timeout).
Requires Clojure 1.9 or later.

[![clojars badge](https://img.shields.io/clojars/v/tessellator/resilience4clj-timelimiter.svg)](https://clojars.org/tessellator/resilience4clj-timelimiter)
[![cljdoc badge](https://cljdoc.org/badge/tessellator/resilience4clj-timelimiter)](https://cljdoc.org/d/tessellator/resilience4clj-timelimiter/CURRENT)


## Quick Start

The following code defines a function `perform-calculation` that uses a time
limiter named `:some-name` and stored in the global registry. If the time
limiter does not already exist, one is created.

```clojure
(ns myproject.some-client
  (:require [resilience4clj.time-limiter :refer [with-time-limiter]])

(defn perform-calculation []
  (with-time-limiter :some-name
    ;; some expensive calculations
  ))
```

Refer to the [configuration guide](/doc/01_configuration.md) for more
information on how to configure the global registry as well as individual
time limiters.

Refer to the [usage guide](/doc/02_usage.md) for more information on how to
use time limiters.

## License

Copyright © 2020 Thomas C. Taylor and contributors.

Distributed under the Eclipse Public License version 2.0.
