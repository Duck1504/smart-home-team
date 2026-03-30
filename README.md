# Smart Home Store

Demo ecommerce cho thiet bi nha thong minh, co khu vuc quan tri rieng.

## Kiem tra yeu cau san pham
- Code: co day du `backend/` (Spring Boot) va `frontend/`.
- Database: co san H2 file local trong `backend/data/` va `data/`.
- ID/PW he thong: co tai khoan admin demo `admin` / `admin123!`.
- MySQL: da bo sung them profile cau hinh va file `docker-compose.mysql.yml` de chay bang MySQL.

## Chay nhanh

### Cach 1: chay voi H2 mac dinh bang Maven
```bash
cd backend
mvn spring-boot:run
```

### Cach 2: dong goi thanh JAR roi chay
```bash
cd backend
mvn package
java -jar target/smart-home-backend-0.1.0.jar
```

## Chay voi MySQL

### Cach 1: dung Docker Compose
```bash
docker compose -f docker-compose.mysql.yml up -d
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Thong so MySQL mac dinh:
- host: `localhost`
- port: `3306`
- database: `smarthome_store`
- username: `smarthome`
- password: `smarthome123`
- root password: `root123`

### Cach 2: dung MySQL co san tren may
1. Tao database `smarthome_store` trong MySQL.
2. Copy thong so trong `backend/.env.mysql.example` vao bien moi truong that.
3. Chay backend voi profile `mysql`.

Vi du PowerShell:
```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:APP_DATASOURCE_URL="jdbc:mysql://localhost:3306/smarthome_store?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false"
$env:APP_DATASOURCE_USERNAME="smarthome"
$env:APP_DATASOURCE_PASSWORD="smarthome123"
cd backend
mvn spring-boot:run
```

Ghi chu:
- H2 van la mac dinh, nen neu khong bat profile `mysql` thi he thong se chay bang database file local.
- Hibernate dang de `update`, nen bang MySQL se duoc tao/cap nhat tu entity khi app khoi dong.
- Tai khoan admin demo `admin` / `admin123!` se duoc seed tu dong neu chua ton tai.

## Dia chi mac dinh
- Trang chu: `http://localhost:8080/index.html`
- Gio hang: `http://localhost:8080/cart.html`
- Dang nhap: `http://localhost:8080/login.html`
- Dang ky: `http://localhost:8080/register.html`
- Quen mat khau: `http://localhost:8080/forgot-password.html`
- Quan tri san pham: `http://localhost:8080/admin-products.html`
- Quan tri danh muc: `http://localhost:8080/admin-categories.html`
- Quan ly user: `http://localhost:8080/admin-users.html`
- Thong ke ban hang: `http://localhost:8080/admin-stats.html`

## Tai khoan demo
- Admin: `admin`
- Password: `admin123!`

## Chuc nang

### Nguoi dung
- Dang ky
- Dang nhap
- Quen mat khau theo username (demo local)
- Xem san pham
- Loc theo danh muc
- Tim kiem san pham
- Them vao gio hang
- Dat hang
- Xem lich su don hang

### Quan tri
- CRUD san pham
- CRUD danh muc
- Quan ly user
- Dat lai mat khau cho user
- Phan quyen USER / ADMIN
- Xem thong ke hang hoa ban ra va ton kho

## Phan quyen
- `USER`: mua hang, tao don, xem don cua chinh minh
- `ADMIN`: truy cap toan bo API `/api/admin/**` va cac trang quan tri frontend

## Frontend tach rieng
Thu muc `frontend/` duoc dong bo tu `backend/src/main/resources/static/`.
Neu chay frontend tach rieng, JS se tu goi backend o `localhost:8080`.
