function decodeBase64Url(value: string): ArrayBuffer {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
  const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
  const binary = globalThis.atob(padded);
  const bytes = Uint8Array.from(binary, char => char.charCodeAt(0));
  return bytes.buffer;
}

function encodeBase64Url(buffer: ArrayBuffer | null): string | undefined {
  if (!buffer) {
    return undefined;
  }

  const binary = Array.from(new Uint8Array(buffer))
    .map(byte => String.fromCharCode(byte))
    .join('');

  return globalThis.btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

function mapCredentialDescriptors(
  descriptors?: Array<{ id: string } & Record<string, unknown>>
): PublicKeyCredentialDescriptor[] | undefined {
  return descriptors?.map(descriptor => ({
    ...descriptor,
    id: decodeBase64Url(descriptor.id)
  })) as PublicKeyCredentialDescriptor[] | undefined;
}

export function supportsPasskeys(win: Window | null | undefined): boolean {
  return Boolean(
    win &&
    win.isSecureContext &&
    typeof globalThis.PublicKeyCredential !== 'undefined' &&
    win.navigator?.credentials
  );
}

export function toRegistrationOptions(
  options: Record<string, unknown>
): CredentialCreationOptions {
  const publicKey = options as Record<string, unknown>;
  const user = publicKey['user'] as Record<string, unknown>;

  return {
    publicKey: {
      ...publicKey,
      challenge: decodeBase64Url(String(publicKey['challenge'])),
      user: {
        ...user,
        id: decodeBase64Url(String(user['id']))
      },
      excludeCredentials: mapCredentialDescriptors(
        publicKey['excludeCredentials'] as Array<{ id: string } & Record<string, unknown>> | undefined
      )
    } as PublicKeyCredentialCreationOptions
  };
}

export function toAuthenticationOptions(
  options: Record<string, unknown>
): CredentialRequestOptions {
  const publicKey = options as Record<string, unknown>;

  return {
    publicKey: {
      ...publicKey,
      challenge: decodeBase64Url(String(publicKey['challenge'])),
      allowCredentials: mapCredentialDescriptors(
        publicKey['allowCredentials'] as Array<{ id: string } & Record<string, unknown>> | undefined
      )
    } as PublicKeyCredentialRequestOptions
  };
}

export function serializePublicKeyCredential(credential: PublicKeyCredential): Record<string, unknown> {
  const response = credential.response as AuthenticatorResponse & Record<string, unknown>;
  const base = {
    id: credential.id,
    rawId: encodeBase64Url(credential.rawId),
    type: credential.type,
    authenticatorAttachment: (credential as PublicKeyCredential & { authenticatorAttachment?: string | null })
      .authenticatorAttachment ?? null,
    clientExtensionResults: credential.getClientExtensionResults()
  };

  if ('attestationObject' in response) {
    return {
      ...base,
      response: {
        clientDataJSON: encodeBase64Url(response.clientDataJSON as ArrayBuffer),
        attestationObject: encodeBase64Url(response['attestationObject'] as ArrayBuffer),
        transports: (response['getTransports'] as (() => string[]) | undefined)?.() ?? []
      }
    };
  }

  return {
    ...base,
    response: {
      clientDataJSON: encodeBase64Url(response.clientDataJSON as ArrayBuffer),
      authenticatorData: encodeBase64Url(response['authenticatorData'] as ArrayBuffer),
      signature: encodeBase64Url(response['signature'] as ArrayBuffer),
      userHandle: encodeBase64Url((response['userHandle'] as ArrayBuffer | null | undefined) ?? null)
    }
  };
}
