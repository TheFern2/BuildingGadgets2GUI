# Schematic API Plan

## Overview

This document outlines the API endpoints needed for saving and loading BuildingGadgets2GUI schematics to/from a remote server. Since schematics use compressed binary NBT format (`.bg2schem`), the API must handle binary file transfers.

## File Format Reference

Our schematics are stored as **compressed NBT** files with the following structure:

```
CompoundTag {
  version: int
  blockCount: int
  copyUUID: string (optional)
  metadata: CompoundTag {
    name: string
    description: string (optional)
    created: long (timestamp ms)
    modified: long (timestamp ms)
    author: string (optional)
    tags: string (comma-separated)
  }
  dimensions: CompoundTag {
    x: int
    y: int
    z: int
  }
  blocks: CompoundTag (BG2Data format)
  tedata: ListTag<CompoundTag> (tile entity data, optional)
}
```

## Authentication

The API uses Bearer token authentication. Tokens are obtained by creating an API key through the developer portal or via the token endpoints below.

### Authentication Header

For protected endpoints, include the Bearer token in the `Authorization` header:

```
Authorization: Bearer <your_api_token>
```

### Token Types

| Type | Lifetime | Use Case |
|------|----------|----------|
| **API Key** | Long-lived (until revoked) | Server-to-server, CLI tools, mod integration |
| **Session Token** | Short-lived (24h) | Web dashboard, temporary access |

---

## Authentication Endpoints

### 1. Create API Token

**Endpoint:** `POST /auth/tokens`

**Description:** Create a new API token for programmatic access.

**Authentication:** Bearer token (existing token with `token:create` scope) or session cookie

**Content-Type:** `application/json`

**Request:**
```json
{
  "name": "My Minecraft Mod",
  "scopes": ["schematics:read", "schematics:write", "schematics:delete"],
  "expiresIn": null
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Descriptive name for the token |
| `scopes` | string[] | No | Permissions (default: all user permissions) |
| `expiresIn` | int | No | Expiration in seconds (`null` = never) |

**Available Scopes:**
- `schematics:read` - View and download schematics
- `schematics:write` - Upload and update schematics
- `schematics:delete` - Delete schematics
- `tokens:read` - List own tokens
- `tokens:create` - Create new tokens
- `tokens:revoke` - Revoke tokens
- `profile:read` - Read own profile
- `profile:write` - Update own profile

**Response:** `201 Created`
```json
{
  "id": "tok_abc123def456",
  "name": "My Minecraft Mod",
  "token": "bg2_live_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "scopes": ["schematics:read", "schematics:write", "schematics:delete"],
  "createdAt": 1706900000000,
  "expiresAt": null,
  "lastUsedAt": null
}
```

**Important:** The `token` field is only returned once at creation. Store it securely.

**Error Responses:**
- `400 Bad Request` - Invalid scope or parameters
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions to create token with requested scopes

---

### 2. List API Tokens

**Endpoint:** `GET /auth/tokens`

**Description:** List all API tokens for the authenticated user.

**Authentication:** Required (Bearer token with `tokens:read` scope)

**Response:** `200 OK`
```json
{
  "tokens": [
    {
      "id": "tok_abc123def456",
      "name": "My Minecraft Mod",
      "scopes": ["schematics:read", "schematics:write"],
      "createdAt": 1706900000000,
      "expiresAt": null,
      "lastUsedAt": 1706950000000,
      "lastUsedIp": "192.168.1.100"
    },
    {
      "id": "tok_xyz789ghi012",
      "name": "CLI Tool",
      "scopes": ["schematics:read"],
      "createdAt": 1706800000000,
      "expiresAt": 1709400000000,
      "lastUsedAt": 1706850000000,
      "lastUsedIp": "10.0.0.50"
    }
  ]
}
```

---

### 3. Get Token Info

**Endpoint:** `GET /auth/tokens/{tokenId}`

**Description:** Get details about a specific token.

**Authentication:** Required (Bearer token with `tokens:read` scope)

**Response:** `200 OK`
```json
{
  "id": "tok_abc123def456",
  "name": "My Minecraft Mod",
  "scopes": ["schematics:read", "schematics:write"],
  "createdAt": 1706900000000,
  "expiresAt": null,
  "lastUsedAt": 1706950000000,
  "lastUsedIp": "192.168.1.100"
}
```

---

### 4. Revoke Token

**Endpoint:** `DELETE /auth/tokens/{tokenId}`

**Description:** Revoke/delete an API token.

**Authentication:** Required (Bearer token with `tokens:revoke` scope)

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Cannot revoke this token
- `404 Not Found` - Token doesn't exist

---

### 5. Verify Token

**Endpoint:** `GET /auth/verify`

**Description:** Verify the current token and get associated user info.

**Authentication:** Required (any valid Bearer token)

**Response:** `200 OK`
```json
{
  "valid": true,
  "tokenId": "tok_abc123def456",
  "tokenName": "My Minecraft Mod",
  "scopes": ["schematics:read", "schematics:write"],
  "user": {
    "id": "usr_123456",
    "username": "player_username",
    "email": "player@example.com"
  },
  "expiresAt": null
}
```

**Error Response:** `401 Unauthorized`
```json
{
  "valid": false,
  "error": {
    "code": "TOKEN_EXPIRED",
    "message": "The provided token has expired"
  }
}
```

---

### 6. Refresh Token (for session tokens)

**Endpoint:** `POST /auth/refresh`

**Description:** Refresh an expiring session token.

**Authentication:** Required (Bearer token that's within refresh window)

**Response:** `200 OK`
```json
{
  "token": "bg2_sess_newxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "expiresAt": 1707000000000
}
```

---

## API Endpoints

### Base URL
```
https://api.example.com/v1
```

---

### 1. Upload Schematic 🔒

**Endpoint:** `POST /schematics`

**Authentication:** Required (Bearer token with `schematics:write` scope)

**Description:** Upload a new schematic file to the server.

**Content-Type:** `multipart/form-data` or `application/octet-stream`

**Request (multipart/form-data):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | binary | Yes | The `.bg2schem` file |
| `name` | string | No | Display name (extracted from file if not provided) |
| `description` | string | No | Override description |
| `tags` | string[] | No | Additional tags |
| `visibility` | string | No | `public` or `private` (default: `private`) |

**Request (application/octet-stream):**
- Raw binary file in body
- Metadata in headers:
  - `X-Schematic-Name`: Display name
  - `X-Schematic-Description`: Description
  - `X-Schematic-Tags`: Comma-separated tags
  - `X-Schematic-Visibility`: `public` or `private`

**Response:** `201 Created`
```json
{
  "id": "uuid-here",
  "name": "My Castle",
  "description": "A medieval castle",
  "author": "player_username",
  "created": 1706900000000,
  "modified": 1706900000000,
  "tags": ["castle", "medieval"],
  "visibility": "public",
  "dimensions": {
    "x": 45,
    "y": 32,
    "z": 38
  },
  "blockCount": 12847,
  "fileSize": 24576,
  "downloadUrl": "/schematics/uuid-here/download"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid file format or corrupted NBT
- `413 Payload Too Large` - File exceeds size limit
- `415 Unsupported Media Type` - Not a valid `.bg2schem` file

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Token lacks `schematics:write` scope

---

### 2. Download Schematic 🔓

**Endpoint:** `GET /schematics/{id}/download`

**Authentication:** Optional (required for private schematics)

**Description:** Download a schematic file. Public schematics can be downloaded without authentication.

**Response:** `200 OK`
- **Content-Type:** `application/octet-stream`
- **Content-Disposition:** `attachment; filename="schematic_name.bg2schem"`
- **Body:** Raw compressed NBT binary data

**Error Responses:**
- `404 Not Found` - Schematic doesn't exist
- `403 Forbidden` - Private schematic, not owned by requester

**Error Responses:**
- `401 Unauthorized` - Private schematic requires authentication
- `403 Forbidden` - Private schematic, not owned by requester

---

### 3. Get Schematic Metadata 🔓

**Endpoint:** `GET /schematics/{id}`

**Authentication:** Optional (required for private schematics)

**Description:** Get metadata for a schematic without downloading the full file.

**Response:** `200 OK`
```json
{
  "id": "uuid-here",
  "name": "My Castle",
  "description": "A medieval castle",
  "author": "player_username",
  "authorId": "author-uuid",
  "created": 1706900000000,
  "modified": 1706900000000,
  "tags": ["castle", "medieval"],
  "visibility": "public",
  "dimensions": {
    "x": 45,
    "y": 32,
    "z": 38
  },
  "blockCount": 12847,
  "fileSize": 24576,
  "downloads": 142,
  "favorites": 23
}
```

---

### 4. List Schematics 🔓

**Endpoint:** `GET /schematics`

**Authentication:** Optional (required to view own private schematics)

**Description:** List schematics with filtering and pagination. Without authentication, only public schematics are returned.

**Query Parameters:**
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 1 | Page number |
| `limit` | int | 20 | Items per page (max 100) |
| `sort` | string | `modified` | Sort field: `name`, `created`, `modified`, `downloads`, `favorites` |
| `order` | string | `desc` | Sort order: `asc` or `desc` |
| `tags` | string | - | Filter by tags (comma-separated, AND logic) |
| `search` | string | - | Search in name/description |
| `author` | string | - | Filter by author username or ID |
| `visibility` | string | - | Filter: `public`, `private`, or `all` (own schematics) |
| `minBlocks` | int | - | Minimum block count |
| `maxBlocks` | int | - | Maximum block count |

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": "uuid-here",
      "name": "My Castle",
      "author": "player_username",
      "tags": ["castle", "medieval"],
      "dimensions": { "x": 45, "y": 32, "z": 38 },
      "blockCount": 12847,
      "created": 1706900000000,
      "modified": 1706900000000,
      "downloads": 142,
      "thumbnailUrl": "/schematics/uuid-here/thumbnail"
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 156,
    "totalPages": 8
  }
}
```

---

### 5. Update Schematic Metadata 🔒

**Endpoint:** `PATCH /schematics/{id}`

**Authentication:** Required (Bearer token with `schematics:write` scope)

**Description:** Update schematic metadata (not the file itself). Only the owner can update.

**Content-Type:** `application/json`

**Request:**
```json
{
  "name": "Updated Castle Name",
  "description": "New description",
  "tags": ["castle", "medieval", "fortress"],
  "visibility": "public"
}
```

**Response:** `200 OK` (returns updated schematic metadata)

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not the owner of this schematic
- `404 Not Found` - Schematic doesn't exist

---

### 6. Replace Schematic File 🔒

**Endpoint:** `PUT /schematics/{id}/file`

**Authentication:** Required (Bearer token with `schematics:write` scope)

**Description:** Replace the schematic file while keeping the same ID and metadata. Only the owner can replace.

**Content-Type:** `application/octet-stream`

**Request:** Raw binary `.bg2schem` file

**Response:** `200 OK` (returns updated schematic metadata with new dimensions/blockCount)

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not the owner of this schematic
- `404 Not Found` - Schematic doesn't exist

---

### 7. Delete Schematic 🔒

**Endpoint:** `DELETE /schematics/{id}`

**Authentication:** Required (Bearer token with `schematics:delete` scope)

**Description:** Delete a schematic. Only the owner can delete.

**Response:** `204 No Content`

**Error Responses:**
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Not the owner of this schematic
- `404 Not Found` - Schematic doesn't exist

---

### 8. Bulk Download 🔓

**Endpoint:** `POST /schematics/bulk-download`

**Authentication:** Optional (required for private schematics in the list)

**Description:** Download multiple schematics as a ZIP archive. Private schematics will be skipped unless authenticated as owner.

**Content-Type:** `application/json`

**Request:**
```json
{
  "ids": ["uuid-1", "uuid-2", "uuid-3"]
}
```

**Response:** `200 OK`
- **Content-Type:** `application/zip`
- **Content-Disposition:** `attachment; filename="schematics.zip"`
- **X-Skipped-Schematics:** Comma-separated list of IDs that were skipped (private/not found)

---

### 9. Get Tags

**Endpoint:** `GET /tags`

**Authentication:** None required

**Description:** Get all available tags with usage counts (from public schematics only).

**Response:** `200 OK`
```json
{
  "tags": [
    { "name": "castle", "count": 45 },
    { "name": "house", "count": 123 },
    { "name": "medieval", "count": 67 }
  ]
}
```

---

### 10. Get Current User Profile 🔒

**Endpoint:** `GET /users/me`

**Authentication:** Required (Bearer token with `profile:read` scope)

**Description:** Get the profile of the authenticated user.

**Response:** `200 OK`
```json
{
  "id": "usr_123456",
  "username": "player_username",
  "email": "player@example.com",
  "createdAt": 1706800000000,
  "stats": {
    "schematicsCount": 42,
    "publicSchematicsCount": 28,
    "totalDownloads": 1547,
    "totalFavorites": 234,
    "storageUsed": 52428800
  },
  "limits": {
    "maxStorage": 104857600,
    "maxFileSize": 10485760,
    "maxSchematics": 500
  }
}
```

---

## Endpoint Authentication Summary

| Endpoint | Auth | Scope Required |
|----------|------|----------------|
| `POST /auth/tokens` | 🔒 Required | `tokens:create` |
| `GET /auth/tokens` | 🔒 Required | `tokens:read` |
| `GET /auth/tokens/{id}` | 🔒 Required | `tokens:read` |
| `DELETE /auth/tokens/{id}` | 🔒 Required | `tokens:revoke` |
| `GET /auth/verify` | 🔒 Required | Any valid token |
| `POST /auth/refresh` | 🔒 Required | Valid session token |
| `POST /schematics` | 🔒 Required | `schematics:write` |
| `GET /schematics/{id}/download` | 🔓 Optional | `schematics:read` (for private) |
| `GET /schematics/{id}` | 🔓 Optional | `schematics:read` (for private) |
| `GET /schematics` | 🔓 Optional | `schematics:read` (for private) |
| `PATCH /schematics/{id}` | 🔒 Required | `schematics:write` |
| `PUT /schematics/{id}/file` | 🔒 Required | `schematics:write` |
| `DELETE /schematics/{id}` | 🔒 Required | `schematics:delete` |
| `POST /schematics/bulk-download` | 🔓 Optional | `schematics:read` (for private) |
| `GET /tags` | None | - |
| `GET /users/me` | 🔒 Required | `profile:read` |

**Legend:** 🔒 = Required, 🔓 = Optional

---

## Implementation Considerations

### Token Storage in Minecraft Mod

Store the API token securely in the mod's config:

```java
public class ApiConfig {
    private static String apiToken;
    private static final Path TOKEN_FILE = FMLPaths.CONFIGDIR.get()
        .resolve("buildinggadgets2gui/api_token.dat");
    
    public static void saveToken(String token) {
        apiToken = token;
        try {
            // Simple obfuscation (not true encryption, but prevents casual reading)
            byte[] encoded = Base64.getEncoder().encode(token.getBytes(StandardCharsets.UTF_8));
            Files.write(TOKEN_FILE, encoded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getToken() {
        if (apiToken == null) {
            try {
                if (Files.exists(TOKEN_FILE)) {
                    byte[] encoded = Files.readAllBytes(TOKEN_FILE);
                    apiToken = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apiToken;
    }
}
```

### Adding Bearer Token to Requests

```java
public static void uploadSchematic(File file, String name) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    
    byte[] fileBytes = Files.readAllBytes(file.toPath());
    String token = ApiConfig.getToken();
    
    if (token == null || token.isEmpty()) {
        throw new IllegalStateException("API token not configured");
    }
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(API_BASE + "/schematics"))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/octet-stream")
        .header("X-Schematic-Name", name)
        .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
    
    if (response.statusCode() == 401) {
        throw new SecurityException("Invalid or expired API token");
    }
}
```

### Binary Data Handling in Java/Minecraft

When making HTTP requests from a Minecraft mod, use a library that supports binary data:

```java
// Example using Java's HttpClient (Java 11+)
public static void uploadSchematic(File file, String name) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    String token = ApiConfig.getToken();
    
    byte[] fileBytes = Files.readAllBytes(file.toPath());
    
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(API_BASE + "/schematics"))
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/octet-stream")
        .header("X-Schematic-Name", name)
        .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
        .build();
    
    HttpResponse<String> response = client.send(request, 
        HttpResponse.BodyHandlers.ofString());
}

public static void downloadSchematic(String id, File destination) throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    String token = ApiConfig.getToken(); // May be null for public schematics
    
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(API_BASE + "/schematics/" + id + "/download"))
        .GET();
    
    if (token != null && !token.isEmpty()) {
        builder.header("Authorization", "Bearer " + token);
    }
    
    HttpResponse<byte[]> response = client.send(builder.build(),
        HttpResponse.BodyHandlers.ofByteArray());
    
    Files.write(destination.toPath(), response.body());
}
```

### Thread Safety

All HTTP requests MUST be made off the main thread to avoid freezing the game:

```java
CompletableFuture.runAsync(() -> {
    try {
        downloadSchematic(schematicId, targetFile);
        // Update UI on main thread
        Minecraft.getInstance().execute(() -> {
            refreshSchematicList();
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
});
```

### File Validation

Server should validate uploaded files:

1. **Magic bytes check** - Compressed NBT starts with `0x1F 0x8B` (gzip header)
2. **NBT structure validation** - Parse and verify required fields exist
3. **Size limits** - Reasonable max file size (e.g., 10MB)
4. **Block count limits** - Prevent absurdly large schematics

### Caching Strategy

Client-side caching to reduce API calls:

```java
public class SchematicApiCache {
    private static final long CACHE_TTL = 5 * 60 * 1000; // 5 minutes
    
    private Map<String, CachedSchematic> metadataCache = new HashMap<>();
    private File downloadCacheDir;
    
    public SchematicMetadata getMetadata(String id) {
        CachedSchematic cached = metadataCache.get(id);
        if (cached != null && !cached.isExpired()) {
            return cached.metadata;
        }
        // Fetch from API...
    }
}
```

### Error Handling

Standard error response format:

```json
{
  "error": {
    "code": "INVALID_FILE_FORMAT",
    "message": "The uploaded file is not a valid .bg2schem file",
    "details": "NBT parsing failed: unexpected end of stream"
  }
}
```

Common error codes:
- `INVALID_FILE_FORMAT` - Not a valid schematic file
- `FILE_TOO_LARGE` - Exceeds size limit
- `SCHEMATIC_NOT_FOUND` - ID doesn't exist
- `PERMISSION_DENIED` - Can't access private schematic
- `RATE_LIMITED` - Too many requests
- `QUOTA_EXCEEDED` - Storage quota exceeded
- `TOKEN_INVALID` - Token is malformed or doesn't exist
- `TOKEN_EXPIRED` - Token has expired
- `TOKEN_REVOKED` - Token has been revoked
- `INSUFFICIENT_SCOPE` - Token lacks required scope for this operation
- `AUTHENTICATION_REQUIRED` - Endpoint requires authentication

### Rate Limiting

Recommended limits:
- Uploads: 10 per minute
- Downloads: 60 per minute
- Metadata requests: 120 per minute

Response headers:
```
X-RateLimit-Limit: 60
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1706900060
```

---

## Future Considerations

### Thumbnails/Previews
- Server-side rendering of schematic previews
- Endpoint: `GET /schematics/{id}/thumbnail`
- Consider using a lightweight voxel renderer

### Versioning
- Track schematic versions/history
- Endpoint: `GET /schematics/{id}/versions`
- Allow rollback to previous versions

### Collections/Folders
- Group schematics into collections
- Endpoint: `POST /collections`, `GET /collections/{id}/schematics`

### Social Features
- Favorites: `POST /schematics/{id}/favorite`
- Comments: `GET/POST /schematics/{id}/comments`
- Ratings: `POST /schematics/{id}/rate`

### Sharing
- Generate shareable links
- Endpoint: `POST /schematics/{id}/share`
- Time-limited or permanent share tokens
