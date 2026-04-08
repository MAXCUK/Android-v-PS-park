# XBoard Integration Sketch for NekoBoxForAndroid

## Goal
Use NekoBox's existing subscription/group/profile pipeline as the connection backbone,
while replacing generic subscription import with a dedicated XBoard sync flow.

## Best insertion point
- Keep `ProxyGroup(type = SUBSCRIPTION)`
- Keep `SubscriptionBean`
- Keep `SubscriptionUpdater` periodic update machinery
- Add a custom updater path keyed by XBoard-style subscription links or a dedicated marker

## Proposed components
1. `xboard/XBoardApiClient`
   - login(email, password)
   - getSubscribe(auth)
   - fetchSubscriptionRaw(subscribeUrl, userAgent=v2rayNG/1.8.0)
2. `xboard/XBoardSyncManager`
   - ensureGroup(panelName)
   - save/update SubscriptionBean
   - trigger GroupUpdater or direct RawUpdater.parseRaw(raw)
3. `xboard/XBoardBeanMapper`
   - optional if direct RawUpdater.parseRaw(raw) is enough
   - only needed if we merge summary metadata from `/user/server/fetch`
4. `xboard/XBoardSessionStore`
   - persist email / auth_data / panel metadata

## Simplest first implementation
- After login:
  - call `getSubscribe`
  - create/update a dedicated `ProxyGroup`
  - set `group.subscription.link = subscribe_url`
  - set `group.subscription.customUserAgent = "v2rayNG/1.8.0"`
  - call `GroupUpdater.startUpdate(group, true)`

This works because NekoBox already has:
- `SubscriptionUpdater`
- `RawUpdater.doUpdate(...)`
- `RawUpdater.parseRaw(...)`
- profile persistence (`ProxyEntity`)
- connection runtime (`ProxyEntity -> buildConfig -> Libcore`)

## Important note
For XBoard dedicated UX, we should hide generic import UI later,
but the first milestone does NOT need to rewrite NekoBox core subscription logic.
