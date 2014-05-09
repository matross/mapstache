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

in your `project.clj` dependencies. It is avaliable for download via Clojars.

_NOTE_: Mapstache does not provide a template engine. In order to use it,
you must provide your own implementation of `matross.mapstache/IRender`.

An example with Clostache would look like:

```clj
(require `[[matross.mapstache :refer [IRender mapstache]]
           [clostache.parser :as mustache]])

(defn mustached [m]
  (mapstache
   (reify IRender
     (render [_ s d] (mustache/render s d)))
   m))

(mustached {:key "value" :other-key "{{key}}"})
```

## License

Copyright © 2014 Darrell Hamilton

Distributed under the MIT License
