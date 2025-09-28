Unused parameters are misleading. Whatever the values passed to such parameters, the behavior will be the same.

## Noncompliant Code Example ##

    function doSomething(a : int, b : int) {     // "b" is unused
      compute(a)
    }

## Compliant Solution ##

    function doSomething(a : int) {
      compute(a)
    }

## Exceptions ##
The rule will not raise issues for unused parameters:

 *  in overridden methods
 *  in non-private methods that only `throw` or that have empty bodies
 *  in overridable methods (non-final, or not member of a final class, non-static, non-private), if the parameter is documented with a proper javadoc.
 *  in annotated methods, unless the annotation is `@SuppressWarning("unchecked")` or `@SuppressWarning("rawtypes")`, in which case the annotation will be ignored (disabled by default)