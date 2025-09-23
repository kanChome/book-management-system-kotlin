# Book Management System (Kotlin/Spring Boot)

Kotlin + Spring Boot による書籍・著者管理アプリです。buckpal に倣ったクリーンアーキテクチャを採用し、アプリケーション層の UseCase（@Service）をトランザクション境界とし、外部I/O は application.port.out を介してアダプタ（jOOQ）へ委譲します。

## 必要ソフトウェア
- JDK 21
- Docker / Docker Compose
- Gradle 8 以降（ラッパー使用可）
- Git（フックを使う場合）

## 初期セットアップ
```bash
# 1) DB 起動
docker compose up -d postgres

# 2) スキーマ適用 + jOOQ コード生成
./gradlew flywayMigrate generateJooq

# 3) ktlint の pre-commit フックを登録
./gradlew addKtlintCheckGitPreCommitHook

# 4) アプリ起動（local プロファイル）
./gradlew bootRun
# => http://localhost:8080
```

## API（概要）
- Books
  - POST `/api/books` （登録）
  - PUT `/api/books/{id}` （更新）
  - GET `/api/books?authorId={UUID}` （著者別検索）
- Authors
  - POST `/api/authors` （登録）
  - PUT `/api/authors/{id}` （更新）
- エラーレスポンス（例）
```json
{
  "timestamp": "2025-01-01T12:34:56Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/books",
  "fieldErrors": [{ "field": "title", "message": "must not be blank" }]
}
```

## アーキテクチャ概要
- application
  - `*.input.*` … UseCase インターフェイス
  - `*.service.*` … UseCase 実装（@Service / @Transactional）
  - `*.port.out.*` … Outgoing Port（Load*/Save*/Query* 単一責務）
- domain
  - モデル・ドメインサービス（Spring 依存なし）
- adapter/out/persistence
  - jOOQ による RDB アダプタ（PostgreSQL）
- interfaces/api
  - REST コントローラ + GlobalExceptionHandler

## ディレクトリ（抜粋）
```
src/main/kotlin/com/example/demo/
  application/... (usecases, ports)
  domain/...      (entities, services)
  book/adapter/out/persistence/JooqBookPersistenceAdapter.kt
  author/adapter/out/persistence/JooqAuthorPersistenceAdapter.kt
  interfaces/api/... (controllers, exception handler)
```

## DB とコード生成
- マイグレーション: `src/main/resources/db/migration`
- 生成物: `build/generated-src/jooq/main/...`（ktlint 対象外）
- スキーマ変更時: `./gradlew flywayMigrate generateJooq`

## 実行・テスト
- 実行: `./gradlew bootRun`
- テスト: `./gradlew test`（Testcontainers で PostgreSQL を自動起動）

## 設定（主なプロパティ）
- `spring.datasource.*`（compose の postgres に接続）
- `spring.jooq.sql-dialect=POSTGRES`
- `spring.flyway.*`（clean-disabled=true）

## コード品質
- チェック: `./gradlew ktlintCheck`
- 整形: `./gradlew ktlintFormat`
- pre-commit フック: `./gradlew addKtlintCheckGitPreCommitHook`

## トラブルシュート
- jOOQ 生成に失敗: DB が起動しているか / Flyway 適用後に `generateJooq` を実行
- Testcontainers が失敗: Docker が起動しているか / ポート競合を確認
- ktlint エラー: `./gradlew ktlintFormat`

---
