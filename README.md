# Mapstache

Mapstache is a specialized map implementation bent on making end user configuration
files more expressive. Treating all string values as templates and rendering them
before returning their value is it's defining feature. This allows end users to
easily build new pieces of configuration data off of existing ones.

## Installation

You can use Mapstache by including

```clj
[matross/mapstache "0.1.1"]
```

in your `project.clj` dependencies. It is avaliable for download via [Clojars](https://clojars.org/matross/mapstache).

**NOTE**: Mapstache does not provide a template engine. In order to use it,
you must provide your own implementation of `matross.mapstache/IRender`.

For example, if you wanted to use [Clostache](https://github.com/fhd/clostache) as your underlying renderer,
it might look something like:

```clj
(require '[matross.mapstache :refer [IRender mapstache]]
         '[clostache.parser :as mustache])

(defn mustached [m]
  (mapstache
   (reify IRender
     (render [_ s d] (mustache/render s d)))
   m))

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
we're using Clostache as described above, we could refactor our config using the [mustache](http://mustache.github.io/)
template syntax:

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
