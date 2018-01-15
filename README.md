# duct-simple-mailer
Very simple email sender component for Duct framework

[![CircleCI](https://circleci.com/gh/mariusz-jachimowicz-83/duct-simple-mailer.svg?style=svg)](https://circleci.com/gh/mariusz-jachimowicz-83/duct-simple-mailer)

## Installation

[![Clojars Project](https://img.shields.io/clojars/v/com.mjachimowicz/duct-simple-mailer.svg)](https://clojars.org/com.mjachimowicz/duct-simple-mailer)

## Usage

To add this module to your configuration, add the `:dsm/module` key:

```clojure
{:dsm/module {}}
```
It will add scratch mailer configuration in to the duct system.  
To configure mailer you need alterate configuration by:

```clojure
:dsm/mailer
{:type     nil ;; "smtp-mailer" or "test-mailer" 
 :host     nil
 :user     nil
 :pass     nil
 :ssl      true ;; or false
 :port     nil
 :tls      nil
 :from     nil
 :reply-to nil}
```

You can use your private gmail account to send emails. See examples from `postal` library.

## License

Copyright © 2018 Mariusz Jachimowicz

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
