# Mapstache

Mapstache provides a specialized map implementation bent on making end user configuration
files more expressive. All string values stored in the map will be rendered as templates before
their value is returned. This provides a simple mechanism to reuse existing configuration data
to build new values.

## Installation

You can use Mapstache by including

```clj
[matross/mapstache "0.2.1"]
```

in your `project.clj` dependencies. It is avaliable for download via [Clojars](https://clojars.org/matross/mapstache).

**NOTE**: Mapstache does not provide an `IRender` (template engine) implementation. In order to use it,
you must provide your own implementation of `matross.mapstache/IRender`.

For example, if you wanted to use [Stencil](https://github.com/davidsantiago/stencil) as your underlying renderer,
it might look something like:

```clj
(require '[matross.mapstache :refer [string-renderer mapstache]]
          '[stencil.core :as stencil])

(defn mustached [m]
  (let [renderer (string-renderer stencil/render-string)]
    (mapstache renderer m)))

(mustached {:key "value" :other-key "{{key}}"})
```

## Usage

Mapstache is primarily focused on making end user configuration more expressive. So, let's start with an imaginary yaml configuration:

```yaml
---
base_url: "http://example.com"
web_root: "/my-app"
health_check_url: "http://example.com/my-app/health-check"
```

Any sane developer would want to eliminate this data duplication. This is where Mapstache comes into play. Assuming
we're using Stencil as described above, we could refactor our config using the [mustache template syntax](http://mustache.github.io/):

```yaml
---
base_url: "http://example.com"
web_root: "/my-app"
health_check_url: "{{base_url}}{{web_root}}/health-check"
```

Now, when you query `:health_check_url`, you get exactly what you expect:

```clj
(:health_check_url config)
; => "http://example.com/my-app/health-check"
```

And that's it!

## License

Copyright © 2014 Darrell Hamilton

Distributed under the MIT License
