interface LoginConfig {
    hostname: string;
    port: number;
    username: string;
    password: string;
    authType: number;
}

interface Folder {
    path: string;
    flags: number;
}

interface Attachment {
    filename: string;
    size: string;
    encoding: number;
    uniqueId: string;
}

interface InlineAttachment extends Attachment {
    cid: string;
    partID: string;
    mimepart: string;
}

interface Mail {
    headers: {[key: string]: string};
    id: number;
    flags: number;
    from: string;
    subject: string;
    date: string;
    attachments: string;
}

interface MailDetail extends Omit<Omit<Mail, "attachments">, "from"> {
    from: {mailbox: string; displayName: string};
    to: {[key: string]: string};
    cc?: {[key: string]: string};
    bcc?: {[key: string]: string};
    body: string;
    attachments: {[key: number]: Attachment};
    inline: {[key: number]: InlineAttachment};
}

interface FolderStatus {
    unseenCount: number;
    messageCount: number;
    recentCount: number;
}

type Response<T> = Promise<T & {status: string}>;

export function loginImap(config: LoginConfig): Response<{}>;

export function loginSmtp(config: LoginConfig): Response<{}>;

export function createFolder({folder}: {folder: string}): Response<{}>;

export function renameFolder({
    folderOldName,
    folderNewName,
}: {
    folderOldName: string;
    folderNewName: string;
}): Response<{}>;

export function deleteFolder({folder}: {folder: string}): Response<{}>;

export function getFolders(): Response<{folders: Folder[]}>;

export function moveEmail({
    folderFrom,
    folderTo,
    messageId,
}: {
    folderFrom: string;
    folderTo: string;
    messageId: number;
}): Response<{}>;

export function permanentDeleteEmail({
    folderFrom,
    messageId,
}: {
    folderFrom: string;
    messageId: number;
}): Response<{}>;

export function getMails({
    folder,
    requestKind,
}: {
    folder: string;
    requestKind: number;
}): Response<{mails: Mail[]}>;

export function getMail({
    folder,
    messageId,
    requestKind,
}: {
    folder: string;
    messageId: number;
    requestKind: number;
}): Response<MailDetail>;

export function sendMail({}: {
    headers?: {[key: string]: string};
    from: {addressWithDisplayName: string; mailbox: string};
    to: {[mailbox: string]: string};
    cc?: {[mailbox: string]: string};
    bcc?: {[mailbox: string]: string};
    subject?: string;
    body: string;
    attachments?: string[];
}): Response<{}>;

export function statusFolder({}: {
    folder: string;
}): Response<FolderStatus>;