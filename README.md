# MineVN Library

Hỗ trợ phát triển plugin Minecraft nhanh chóng, hướng đến mô hình MVC

Hỗ trợ kết nối nhanh Database, tạo GUI, viết command...

Ngôn ngữ hỗ trợ: Kotlin  
Phiên bản Java: 17
Phiên bản Minecraft: 1.8+
- Các phiên bản đã test: 1.12.2, 1.20.4

<details><summary>Các thư viện bên ngoài</summary>

- HikariCP
- Gson
- H2 driver
- MySQL

</details>

## Cách sử dụng

Thêm MineVNLib vào project qua Gradle  
(2024 rồi đừng dùng maven nữa bạn nhé)

```kotlin
repositories {
    maven {
        setUrl("http://pack.minevn.net/repo/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    compileOnly("minevn.depend:minevnlib:1.0.0")
}
```

Trong trường hợp `http://pack.minevn.net/repo/` bị sập, hãy lấy file jar trong release nhé.

Tiếp theo cho Main class của bạn kế thừa `MineVNPlugin` thay vì `JavaPlugin`  
Superclass này cung cấp các method liên quan đến database và sẽ lưu giữ connection pool của plugin.

```kotlin
class MyPlugin : MineVNPlugin() { ... }
```


### Kết nối Database

Driver hỗ trợ: H2, MySQL, MariaDB với HikariCP.

Trước tiên thì bạn cần một file config như sau:

```yaml
database:
  # engine mặc định sử dụng h2 (lưu vào 1 file), có thể chuyển sang mysql
  engine: h2
  h2:
    file: dotman
  mysql:
    host: localhost
    port: 3306
    user: 'root'
    password: '123'
    database: dotman
```

Hãy lấy section `database` để gọi hàm sau trong main class:

```kotlin
initDatabase(config.getConfigurationSection("database"))
```

Giả sử bạn chỉ muốn plugin hỗ trợ H2 và chưa làm thêm MariaDB, MySQL thì hãy kiểm tra config `engine` trước khi kết nối.

Hãy xem [code tham khảo](https://github.com/minevn/dotman/blob/master/dotman-plugin/src/main/java/net/minevn/dotman/DotMan.kt#L64).

### Tương tác với database
Trước tiên hãy tìm hiểu về khái niệm [Data Access Object (DAO)](https://gpcoder.com/4935-huong-dan-java-design-pattern-dao/).

Bạn sẽ đặt các class DAO như sau:

![sample](https://i.imgur.com/IA345j6.png)

Bên trong các `DAOImpl`, thực thi câu lệnh SQL theo lối viết DSL:
```kotlin
fun getPlayers(groupId: Int) : List<MyPlayer> {
    return "SELECT * FROM PLAYERS WHERE GROUP_ID = ?".statement {
        setInt(1, groupId)
        fetchRecords {
            MyPlayer(getInt("id"), getString("name"), getInt("group_id"))
        }
    }
}
```

Trong đó:
- Hàm `statement` sẽ tạo ra PreparedStatement và thực thi nó
- Hàm `fetchRecords` sẽ lấy kết quả trả về từ PreparedStatement và chuyển nó thành List các object
  - Còn một số hàm khác bên cạnh `fetchRecords`, chi tiết xem tại [đây](https://github.com/MineVN/minevn-library/blob/master/minevnlib-master/src/main/java/net/minevn/libs/db/DataAccess.kt).

Từ đây bạn có thể lấy instance của các DAO qua hàm `plugin.getDAO(MyDAO::class.java)` và sử dụng.

Tham khảo code mẫu tại [đây](https://github.com/minevn/dotman/tree/master/dotman-plugin/src/main/java/net/minevn/dotman/database).

### Đánh version cho Database
Còn được gọi là Schema versioning/migration.

Thông thường thì khi chạy plugin lần đầu, nó sẽ tạo một số bảng cần thiết, và sau này sẽ còn cập nhật thêm bảng mới, cột mới cho các bảng nữa.

Thư viện này sẽ hỗ trợ bạn dễ dàng làm việc trên.

Đầu tiên hãy tạo các file sql đánh version theo format `0001.sql` tại thư mục `resource/db/migrations` cho các datasource như ảnh:

![sample](https://i.imgur.com/weivUOH.png)

Bên trong file sql của từng datasource, hãy viết lệnh tạo bảng cần thiết

![sample](https://i.imgur.com/BcGtihZ.png)

Sau đó thực hiện cập nhật và đánh version

```kotlin
val configDao = getDAO(ConfigDAO::class)
// lấy version hiện tại
val schemaVersion = configDao.get("migration_version") ?: "0"
// đường dẫn đến các file sql
val path = "db/migrations/${dbPool!!.getTypeName()}"
// tạo lấy connection từ pool rồi thực hiện cập nhật 
val updated = dbPool!!.getConnection().use {
    BukkitDBMigrator(this, it, path, schemaVersion.toInt()).migrate()
}
// lưu lại version sau khi cập nhật
configDao.set("migration_version", updated.toString())
```

Sau này bạn tạo thêm file `0002.sql`, `0003.sql`... để thêm cột mới, bảng mới, thay đổi cấu trúc bảng, plugin sẽ tự động cập nhật và đánh version.

### Transactional
Cách hoạt động tương đương với `@Transactional` của Spring.

Khi bạn tạo một transaction, tất cả các câu lệnh SQL trong block sẽ được thực hiện trong một transaction, nếu có lỗi xảy ra, toàn bộ transaction sẽ rollback.

Nói đơn giản là trong transaction mà có exception xảy ra thì sẽ không có một câu lệnh update, insert nào được thực hiện.

```kotlin
transactional(plugin.dbPool!!.getConnection()) {
    // Lấy DAO và thực hiện cập nhật thông qua DAO tại đây...
    // Sau khi hoàn tất transaction, connection sẽ tự commit, hoặc tự rollback nếu có exception.
}
```

### Command DSL

Đang bổ sung tài liệu.

Xem [code mẫu](https://github.com/minevn/dotman/blob/master/dotman-plugin/src/main/java/net/minevn/dotman/commands/AdminCmd.kt).

### GUI

Đang bổ sung tài liệu.

Xem [code mẫu](https://github.com/MineVN/dotman/blob/master/dotman-plugin/src/main/java/net/minevn/dotman/gui/CardPriceUI.kt).

### Dialog

Mở dialog (tính năng của Minecraft 1.21.6) qua `DialogAPI`, dùng chung cho mọi phiên bản server:
- `DialogBody.RichText` khai báo từng đoạn chữ (font, màu, bóng, click, tooltip), dùng khi Component không đủ
  - Adventure của server cũ chưa biết shadow color và click custom
  - Dialog đồ hoạ của matchmaker dựng trên kiểu thân này
  - Callback trong chữ nhận `values` rỗng, vì click chữ không gửi được giá trị input
- Server có dialog API của Paper (Paper 1.21.7+): gọi API của Paper
  - `clear` cần Paper 1.21.8+
- Server cũ hơn 1.21.6 có ViaVersion 5.11.0+: dựng packet dialog qua ViaVersion, chỉ client 1.21.6+ xem được
- Server cũ hơn 1.21.6 không có ViaVersion: ném `ViaVersionNotInstalledException`
- Server 1.21.6+ không có dialog API của Paper (Spigot, Paper 1.21.6): ném `UnsupportedOperationException`

```kotlin
if (!DialogAPI.isSupported(player)) {
    // fallback: chest GUI / chat
    return
}
DialogAPI.show(player, NoticeDialog(
    title = text("Đổi tên"),
    inputs = listOf(DialogInput.Text("name", text("Tên mới"), maxLength = 16)),
    action = DialogButton(text("Lưu"), DialogAction.Callback { response ->
        val name = response.string("name")
    }),
))
```

Giới hạn:
- Model dùng Adventure `Component`, nên cần server Paper (hoặc server có Adventure)
- Chỉ hỗ trợ dialog `notice`, `confirmation`, `multi_action` và body `plain_message`
- Đường ViaVersion:
  - Callback nhận qua packet `custom_click_action` bằng cách ghi đè handler trong ViaVersion
  - Nếu hook lỗi (thường do nâng ViaVersion), callback chuyển sang lệnh ẩn `/mvndialogcb`
    - Lệnh bị giới hạn 256 ký tự, `show` ném `IllegalArgumentException` nếu lệnh có thể dài hơn
    - Lệnh không chứa được ký tự xuống dòng, `show` ném `IllegalArgumentException` nếu dialog có input `multiline`
- Input `multiline` (`DialogInput.Text.Multiline`): packet callback tối đa 65536 byte, tránh đặt `maxLength` quá lớn

Callback (mọi backend):
- Chỉ có hiệu lực với dialog đang mở mới nhất. Dialog có `afterAction` khác `NONE` thì chỉ nhận 1 click
  - Tính cả click trong chữ của `RichText`
- Giá trị input được kiểm tra theo dialog, click có giá trị sai bị bỏ qua:
  - Text bị cắt theo số dòng (1 dòng nếu không có `multiline`) và `maxLength`
  - Bool phải là `onTrue` / `onFalse`, option phải có trong dialog
  - Số bị kẹp vào khoảng `start`..`end`
- Mỗi player tối đa 10 click mỗi giây, click vượt quá bị bỏ qua
- Dialog `WAIT_FOR_RESPONSE`: callback cần mở dialog mới hoặc gọi `DialogAPI.clear` để gỡ màn "Waiting for Server"
  - Callback ném exception thì thư viện tự gỡ màn này
