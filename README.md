# thu-info-mailcore

react native bindings for https://github.com/MailCore/mailcore2, based on [react-native-mailcore](https://github.com/agenthunt/react-native-mailcore), customized for thu-info

> WARNING: For android projects targeting API 30 and above, exclude `'META-INF/NOTICE.md'` and `'META-INF/LICENSE.md'` in your `app/build.gradle`
> 
> For more information, see https://issuetracker.google.com/issues/172544275

## Setup

**Requires `react-native >= 0.61`**

- `yarn add thu-info-mailcore`
- `cd ios && pod install && cd ..` (iOS only)

## Usage

- `loginImap`

- `loginSmtp`

- `createFolder`

- `renameFolder`

- `deleteFolder`

- `getFolders`

- `moveEmail`

- `permanentDeleteEmail`

- `getMails`

- `getMailsByRange`

- `getMail`

- `sendMail`

- `statusFolder`

- `getAttachment`

- `getAttachmentInline`

- `actionFlagMessage`

- `actionLabelMessage`

> For most of the methods you must first use `loginImap`.
> For `sendMail` method you must first use `loginSmtp`.
> For the use of attachments download remember to give permission to the application.

```javascript
import MailCore from "thu-info-mailcore";
```

See [index.d.ts](./index.d.ts) for full definition of these API functions.