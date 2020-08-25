# Security

The security is handled at different levels in the application but
throughout the code base, Spring Security is used to handle this.

Inside services or controllers, you can find annotations that
restrict the ability for a user to execute a whole method. They are
divided in two different categories ; the regular annotations:

* `@PreAuthorize`
* `@IsAdmin`
* `@IsAdminOrDesigner`

While `@PreAuthorize` is provided by Spring Security, `@IsAdmin`
and `@IsAdminOrDesigner` are named explicitly.

And the annotations that are specifically for node records:

* `@CanRead`
* `@CanReadOrPublic`
* `@CanWrite`
* `@CanDelete`
* `@IsAdminOrDesignerOnNode`

The names should be descriptive enough. The only requirement with these
annotations is that the method that rely on them must have a `UUID`
attribute named `id` that represents the ID of the node. For example:

~~~~java
@CanRead
public void doSomethingThatRequireReadRight(@P("id") UUID nodeID) {

}
~~~~

These annotations rely on the fine grained authorization. You can find
more information at the end of this document.

The `UserInfo` class (present in `karuta-business`) holds the information
about the currently logged in user.

You can get an instance of this class from a controller like so:

~~~java
class AController {
    @RequestMapping("/whatever")
    public HttpEntity<SomeDocument> whatever(@AuthenticationPrincipal UserInfo userInfo) {
        // ...
    }
}
~~~

## HTTP authorization

Spring Security is also used to define which URLs are accessible under
which circumstances. You can check out the configuration in the
`SecurityConfiguration` class in the `karuta-webapp` module.

While it seems unlikely, Karuta is actually designed to rely on cookies
for the authorization part rather than relying on a mechanism such as
providing a token.

Thus, you might expect 403 (Forbidden) requests if you don't properly
provide a cookie.

## Fine grained authorization

While users can be defined as admins or designers, the application has a
more fine grained rights checking system for regular users.

`GroupRights` define the actual rights (read, write, etc.) for a given
node and `GroupRightInfo`.

A `GroupRightInfo` holds the information about a group inside a portfolio
for a given `GroupInfo` which is the glue between a `GroupRightInfo` and
a `GroupUser`, the latter representing the belonging of a user to a group.
