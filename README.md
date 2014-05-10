# Mapstache

Mapstache is a specialized map implementation bent on making end user configuration
files more expressive. Treating all string values as templates and rendering them
before returning their value is it's defining feature. This allows end users to
easily build new pieces of configuration data off of existing ones.

## Installation

You can use Mapstache by including

```clj
[matross/mapstache "0.1.0-SNAPSHOT"]
```

in your `project.clj` dependencies. It is avaliable for download via [Clojars](https://clojars.org/matross/mapstache).

**NOTE**: Mapstache does not provide a template engine. In order to use it,
you must provide your own implementation of `matross.mapstache/IRender`.

For example, using Mapstache with [Clostache](https://github.com/fhd/clostache) would look similar to:

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

Now, any sane developer would want to eliminate the data duplication in their configs. This is where Mapstache comes into play. If we were
to pair Mapstache with a [mustache](http://mustache.github.io/) template engine, as in our example above, we can rewrite our config to look like:

```yaml
---
base_url: "http://example.com"
web_root: "/my-app"
health_check_url: "{{base_url}}{{web_root}}/health-check"
```

Using this config with Mapstache and our mustache example above is no different than interacting with any other map, with one exception:

```clj
(def config (mustached my-parsed-yaml))

(:health_check_url config) ; "http://example.com/my-app/health-check"
```

And that's it! As long as your config file can be parsed to a map, your only limitation is the expressiveness of your template engine.

## License

Copyright © 2014 Darrell Hamilton

Distributed under the MIT License
