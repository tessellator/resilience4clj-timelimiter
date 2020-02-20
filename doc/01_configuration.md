## 1. Configuration

### Project Dependencies

resilience4clj-circuitbreaker is distributed through
[Clojars](https://clojars.org) with the identifier
`tessellator/resilience4clj-timelimiter`. You can find the version
information for the latest release at
https://clojars.org/tessellator/resilience4clj-timelimiter.

The library has a hidden dependency on SLF4J, and you will need to include a
dependency and related configuration for a specific logging implementation.

As an example, you could include the following in your project dependencies:

```
[org.slf4j/slf4j-log4j12 <VERSION>]
```

If you do not configure logging, you will see some SLF4J warnings output and
will not receive logs the underlying circuit breaker library.

### Configuration Options

The following table describes the options available when configuring time
limiters as well as default values. A `config` is a map that contains any of the
keys in the table. Note that a `config` without a particular key will use the
default value (e.g., `{}` selects all default values).

| Configuration Option     | Default Value | Description                                                      |
|--------------------------|---------------|------------------------------------------------------------------|
| `:cancel-running-future` |          true | A value indicating whether cancel should be called on the future |
| `:timeout-duration`      |          1000 | The number of milliseconds in the timeout duration               |

A `config` can be used to configure the global registry or a single time limiter
when it is created.

### Global Registry

This library creates a single global `registry` The registry may contain
`config` values as well as time limiter instances.

`configure-registry!` overwrites the existing registry with one containing one
or more config values. `configure-registry!` takes a map of name/config value
pairs. When a time limiter is created, it may refer to one of these names to
use the associated config. Note that the name `:default` (or `"default"`) is
special in that time limiters that are created without a providing or naming
a config with use this default config.

The function `time-limiter!` will look up or create a time limiter in the
global registry. The function accepts a name and optionally the name of a config
or a config map.

```clojure
(ns myproject.core
  (:require [resilience4clj.time-limiter :as tl])

;; The following creates two configs: the default config and the FailFaster
;; config. The default config uses only the defaults and will be used to create
;; time limiters that do not specify a config to use.
(tl/configure-registry! {"default"    {}
                         "FailFaster" {:timeout-duration 500}})


;; create a time limiter named :name using the "default" config from the
;; registry and store the result in the registry
(tl/time-limiter! :name)

;; create a time limiter named :fail-faster using the "FailFaster" config
;; from the registry and store the result in the registry
(tl/time-limiter! :fail-faster "FailFaster")

;; create a time limiter named :custom-config using a custom config map
;; and store the result in the registry
(tl/time-limiter! :custom-config {:cancel-running-future false})
```

### Custom Time Limiters

While convenient, it is not required to use the global registry. You may instead
choose to create time limiters and manage them yourself.

In order to create a time limiter that is not made available globally, use
the `time-limiter` function, which accepts a name and config map.

The following code creates a new time limiter with the default config options.

```clojure
(ns myproject.core
  (:require [resilience4clj.time-limiter :as tl]))

(def my-limiter (tl/time-limiter :my-limiter {}))
```
