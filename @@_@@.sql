-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: localhost    Database: quanlykhoahoc1
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `baitap`
--

DROP TABLE IF EXISTS `baitap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `baitap` (
  `id` int NOT NULL AUTO_INCREMENT,
  `khoaHocID` int DEFAULT NULL,
  `tenBaiTap` varchar(255) DEFAULT NULL,
  `deadline` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `khoaHocID` (`khoaHocID`),
  CONSTRAINT `baitap_ibfk_1` FOREIGN KEY (`khoaHocID`) REFERENCES `khoahoc` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `baitap`
--

LOCK TABLES `baitap` WRITE;
/*!40000 ALTER TABLE `baitap` DISABLE KEYS */;
INSERT INTO `baitap` VALUES (1,3,'Bài tập 1','2025-04-20 23:59:59'),(2,3,'Bài tập 2','2025-04-25 17:00:00'),(3,9,'Bài ôn tập','2025-05-17 00:00:00');
/*!40000 ALTER TABLE `baitap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cauhoi`
--

DROP TABLE IF EXISTS `cauhoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cauhoi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `noiDung` text NOT NULL,
  `baiTapID` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `baiTapID` (`baiTapID`),
  CONSTRAINT `cauhoi_ibfk_1` FOREIGN KEY (`baiTapID`) REFERENCES `baitap` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cauhoi`
--

LOCK TABLES `cauhoi` WRITE;
/*!40000 ALTER TABLE `cauhoi` DISABLE KEYS */;
INSERT INTO `cauhoi` VALUES (1,'Câu hỏi 1 của Bài 1',1),(2,'Câu hỏi 2 của Bài 1',1),(3,'Câu hỏi 3 của Bài 1',1),(4,'Câu hỏi 4 của Bài 1',1),(5,'Câu hỏi 5 của Bài 1',1),(6,'Câu hỏi 6 của Bài 1',1),(7,'Câu hỏi 1 của Bài 1',2),(8,'Câu hỏi 2 của Bài 1',2),(9,'Câu hỏi 3 của Bài 1',2),(10,'Câu hỏi 4 của Bài 1',2),(11,'Câu hỏi 5 của Bài 1',2),(12,'Câu hỏi 6 của Bài 1',2),(13,'Câu hỏi 1 của Bài 2',2),(14,'Câu hỏi 2 của Bài 2',2),(15,'Câu hỏi 3 của Bài 2',2),(16,'Câu hỏi 4 của Bài 2',2);
/*!40000 ALTER TABLE `cauhoi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chungchi`
--

DROP TABLE IF EXISTS `chungchi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chungchi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hocVienID` int NOT NULL,
  `khoaHocID` int NOT NULL,
  `ngay_phat_hanh` date DEFAULT NULL,
  `ma_chung_chi` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `hocVienID` (`hocVienID`,`khoaHocID`),
  KEY `khoaHocID` (`khoaHocID`),
  CONSTRAINT `chungchi_ibfk_1` FOREIGN KEY (`hocVienID`) REFERENCES `hocvien` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chungchi_ibfk_2` FOREIGN KEY (`khoaHocID`) REFERENCES `khoahoc` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chungchi`
--

LOCK TABLES `chungchi` WRITE;
/*!40000 ALTER TABLE `chungchi` DISABLE KEYS */;
INSERT INTO `chungchi` VALUES (1,1,1,'2025-02-01','CC-JAVA-001'),(2,1,2,'2025-02-02','CC-JAVA-002'),(3,2,3,'2025-02-03','CC-JAVA-003'),(4,2,4,'2025-02-04','CC-JAVA-004'),(5,3,5,'2025-02-05','CC-PYTHON-001'),(6,3,6,'2025-02-06','CC-PYTHON-002'),(7,4,7,'2025-02-07','CC-PYTHON-003'),(8,4,8,'2025-02-08','CC-PYTHON-004'),(9,5,9,'2025-02-09','CC-DATA-001'),(10,5,10,'2025-02-10','CC-DATA-002'),(11,6,11,'2025-02-11','CC-DATA-003'),(12,6,12,'2025-02-12','CC-DATA-004'),(13,7,13,'2025-02-13','CC-WEB-001'),(14,7,14,'2025-02-14','CC-WEB-002'),(15,8,15,'2025-02-15','CC-WEB-003'),(16,8,16,'2025-02-16','CC-WEB-004'),(17,9,17,'2025-02-17','CC-AI-001'),(18,9,18,'2025-02-18','CC-AI-002'),(19,10,19,'2025-02-19','CC-AI-003'),(20,10,20,'2025-02-20','CC-AI-004');
/*!40000 ALTER TABLE `chungchi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dapan`
--

DROP TABLE IF EXISTS `dapan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dapan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `noiDung` text NOT NULL,
  `dapAnDung` tinyint(1) NOT NULL,
  `cauHoiID` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cauHoiID` (`cauHoiID`),
  CONSTRAINT `dapan_ibfk_1` FOREIGN KEY (`cauHoiID`) REFERENCES `cauhoi` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dapan`
--

LOCK TABLES `dapan` WRITE;
/*!40000 ALTER TABLE `dapan` DISABLE KEYS */;
INSERT INTO `dapan` VALUES (1,'Đáp án A - Câu 1',0,1),(2,'Đáp án B - Câu 1',1,1),(3,'Đáp án C - Câu 1',0,1),(4,'Đáp án D - Câu 1',0,1),(5,'Đáp án A - Câu 2',0,2),(6,'Đáp án B - Câu 2',1,2),(7,'Đáp án C - Câu 2',0,2),(8,'Đáp án D - Câu 2',0,2),(9,'Đáp án A - Câu 3',0,3),(10,'Đáp án B - Câu 3',1,3),(11,'Đáp án C - Câu 3',0,3),(12,'Đáp án D - Câu 3',0,3),(13,'Đáp án A - Câu 4',0,4),(14,'Đáp án B - Câu 4',1,4),(15,'Đáp án C - Câu 4',0,4),(16,'Đáp án D - Câu 4',0,4),(17,'Đáp án A - Câu 5',0,5),(18,'Đáp án B - Câu 5',1,5),(19,'Đáp án C - Câu 5',0,5),(20,'Đáp án D - Câu 5',0,5),(21,'Đáp án A - Câu 6',0,6),(22,'Đáp án B - Câu 6',1,6),(23,'Đáp án C - Câu 6',0,6),(24,'Đáp án D - Câu 6',0,6),(25,'Đáp án A - Câu 7',0,7),(26,'Đáp án B - Câu 7',1,7),(27,'Đáp án C - Câu 7',0,7),(28,'Đáp án D - Câu 7',0,7),(29,'Đáp án A - Câu 8',0,8),(30,'Đáp án B - Câu 8',1,8),(31,'Đáp án C - Câu 8',0,8),(32,'Đáp án D - Câu 8',0,8),(33,'Đáp án A - Câu 9',0,9),(34,'Đáp án B - Câu 9',1,9),(35,'Đáp án C - Câu 9',0,9),(36,'Đáp án D - Câu 9',0,9),(37,'Đáp án A - Câu 10',0,10),(38,'Đáp án B - Câu 10',1,10),(39,'Đáp án C - Câu 10',0,10),(40,'Đáp án D - Câu 10',0,10);
/*!40000 ALTER TABLE `dapan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `giangvien`
--

DROP TABLE IF EXISTS `giangvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `giangvien` (
  `id` int NOT NULL,
  `ngay_sinh` date DEFAULT NULL,
  `so_dien_thoai` varchar(15) DEFAULT NULL,
  `dia_chi` text,
  `trinh_do` varchar(100) DEFAULT NULL,
  `nam_kinh_nghiem` int DEFAULT NULL,
  `linh_vuc_chuyen_mon` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `so_dien_thoai` (`so_dien_thoai`),
  CONSTRAINT `giangvien_ibfk_1` FOREIGN KEY (`id`) REFERENCES `nguoidung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `giangvien_chk_1` CHECK ((`nam_kinh_nghiem` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `giangvien`
--

LOCK TABLES `giangvien` WRITE;
/*!40000 ALTER TABLE `giangvien` DISABLE KEYS */;
INSERT INTO `giangvien` VALUES (3,'1980-01-01','0903000001','Hà Nội','Tiến sĩ',10,'Lập trình Java'),(4,'1982-02-02','0903000002','TP.HCM','Thạc sĩ',8,'Lập trình Python'),(5,'1975-03-03','0903000003','Đà Nẵng','Tiến sĩ',15,'Khoa học Dữ liệu'),(6,'1985-04-04','0903000004','Cần Thơ','Thạc sĩ',5,'Lập trình Web'),(7,'1978-05-05','0903000005','Hà Nội','Tiến sĩ',12,'Trí tuệ Nhân tạo');
/*!40000 ALTER TABLE `giangvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hocvien`
--

DROP TABLE IF EXISTS `hocvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hocvien` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nguoiDungID` int NOT NULL,
  `ngay_sinh` date NOT NULL,
  `dia_chi` varchar(255) NOT NULL,
  `gioi_tinh` enum('Nam','Nữ','Khác') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nguoiDungID` (`nguoiDungID`),
  KEY `idx_hocvien_nguoiDungID` (`nguoiDungID`),
  CONSTRAINT `hocvien_ibfk_1` FOREIGN KEY (`nguoiDungID`) REFERENCES `nguoidung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hocvien`
--

LOCK TABLES `hocvien` WRITE;
/*!40000 ALTER TABLE `hocvien` DISABLE KEYS */;
INSERT INTO `hocvien` VALUES (1,8,'2000-01-01','Hà Nội','Nam'),(2,9,'2000-02-02','TP.HCM','Nữ'),(3,10,'2000-03-03','Đà Nẵng','Nam'),(4,11,'2000-04-04','Cần Thơ','Nữ'),(5,12,'2000-05-05','Hà Nội','Nam'),(6,13,'2000-06-06','TP.HCM','Nam'),(7,14,'2000-07-07','Đà Nẵng','Nữ'),(8,15,'2000-08-08','Cần Thơ','Nam'),(9,16,'2000-09-09','Hà Nội','Nữ'),(10,17,'2000-10-10','TP.HCM','Nam'),(11,18,'2000-11-11','Đà Nẵng','Nữ'),(12,19,'2000-12-12','Cần Thơ','Nam'),(13,20,'2001-01-01','Hà Nội','Nữ'),(14,21,'2001-02-02','TP.HCM','Nam'),(15,22,'2001-03-03','Đà Nẵng','Nữ'),(16,23,'2001-04-04','Cần Thơ','Nam'),(17,24,'2001-05-05','Hà Nội','Nữ'),(18,25,'2001-06-06','TP.HCM','Nam'),(19,26,'2001-07-07','Đà Nẵng','Nữ'),(20,27,'2001-08-08','Cần Thơ','Nam'),(21,30,'2001-01-01','Hà Nội','Nam'),(22,34,'2001-01-01','TP.HCM','Nam'),(23,35,'2001-01-01','Đà Nẵng','Nam'),(24,39,'2001-09-09','Dong Nai ','Nam');
/*!40000 ALTER TABLE `hocvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hocvien_baitap`
--

DROP TABLE IF EXISTS `hocvien_baitap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hocvien_baitap` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hocVienID` int DEFAULT NULL,
  `baiTapID` int DEFAULT NULL,
  `diem` int DEFAULT NULL,
  `ngayNop` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `hocVienID` (`hocVienID`),
  KEY `baiTapID` (`baiTapID`),
  CONSTRAINT `hocvien_baitap_ibfk_1` FOREIGN KEY (`hocVienID`) REFERENCES `nguoidung` (`id`),
  CONSTRAINT `hocvien_baitap_ibfk_2` FOREIGN KEY (`baiTapID`) REFERENCES `baitap` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hocvien_baitap`
--

LOCK TABLES `hocvien_baitap` WRITE;
/*!40000 ALTER TABLE `hocvien_baitap` DISABLE KEYS */;
INSERT INTO `hocvien_baitap` VALUES (1,24,1,33,'2025-04-21 20:51:53');
/*!40000 ALTER TABLE `hocvien_baitap` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khoahoc`
--

DROP TABLE IF EXISTS `khoahoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khoahoc` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_khoa_hoc` varchar(255) NOT NULL,
  `mo_ta` text NOT NULL,
  `hinh_anh` varchar(255) DEFAULT NULL,
  `giangVienID` int NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `gia` double DEFAULT NULL,
  `ngay_bat_dau` date DEFAULT NULL,
  `ngay_ket_thuc` date DEFAULT NULL,
  `so_luong_hoc_vien_toi_da` int NOT NULL DEFAULT '40',
  PRIMARY KEY (`id`),
  KEY `giangVienID` (`giangVienID`),
  CONSTRAINT `khoahoc_ibfk_1` FOREIGN KEY (`giangVienID`) REFERENCES `nguoidung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khoahoc`
--

LOCK TABLES `khoahoc` WRITE;
/*!40000 ALTER TABLE `khoahoc` DISABLE KEYS */;
INSERT INTO `khoahoc` VALUES (1,'Java khó','Học lập trình Java cơ bản','/com/ntn/images/courses/img_1.jpg',6,1,500000,'2025-04-16','2025-06-15',40),(2,'Java Nâng Cao','Học lập trình Java nâng cao','/com/ntn/images/courses/img_2.jpg',3,1,650000,'2025-07-01','2025-09-30',40),(3,'Java Web','Học lập trình web với Java','/com/ntn/images/courses/img_3.jpg',3,1,850000,'2025-07-01','2025-09-30',40),(4,'Java Spring','Học framework Spring','/com/ntn/images/courses/img_4.jpg',3,1,150000,'2025-07-01','2025-09-30',40),(5,'Python Cơ Bản','Học lập trình Python cơ bản','/com/ntn/images/courses/img_5.jpg',4,1,430000,'2025-07-01','2025-09-30',40),(6,'Python Nâng Cao','Học lập trình Python nâng cao','/com/ntn/images/courses/img_6.jpg',4,1,200000,'2025-07-01','2025-09-30',40),(7,'Python Web','Học lập trình web với Python','/com/ntn/images/courses/img_7.jpg',4,1,440000,'2025-07-01','2025-09-30',40),(8,'Python AI','Học AI với Python','/com/ntn/images/courses/img_8.jpg',4,1,230000,'2025-07-01','2025-09-30',40),(9,'Khoa học Dữ liệu 1','Giới thiệu khoa học dữ liệu','/com/ntn/images/courses/img_9.jpg',5,1,650000,'2025-07-01','2025-09-30',40),(10,'Khoa học Dữ liệu 2','Phân tích dữ liệu nâng cao','/com/ntn/images/courses/img_10.jpg',5,1,110000,'2025-07-01','2025-09-30',40),(11,'Khoa học Dữ liệu 3','Machine Learning cơ bản','/com/ntn/images/courses/img_11.jpg',5,1,770000,'2025-07-01','2025-09-30',40),(12,'Khoa học Dữ liệu 4','Deep Learning cơ bản','/com/ntn/images/courses/img_12.jpg',5,1,660000,'2025-07-01','2025-09-30',40),(13,'Lập trình Web 1','HTML và CSS cơ bản','/com/ntn/images/courses/img_13.jpg',6,1,200000,'2025-07-01','2025-09-30',40),(14,'Lập trình Web 2','JavaScript cơ bản','/com/ntn/images/courses/img_14.jpg',6,1,200000,'2025-07-01','2025-09-30',40),(15,'Lập trình Web 3','ReactJS cơ bản','/com/ntn/images/courses/img_15.jpg',6,1,200000,'2025-07-01','2025-09-30',40),(16,'Lập trình Web 4','NodeJS cơ bản','/com/ntn/images/courses/img_16.jpg',6,1,200000,'2025-07-01','2025-09-30',40),(17,'AI Cơ Bản','Giới thiệu trí tuệ nhân tạo','/com/ntn/images/courses/img_17.jpg',7,1,200000,'2025-07-01','2025-09-30',40),(18,'AI Nâng Cao','Học máy nâng cao','/com/ntn/images/courses/img_18.jpg',7,1,200000,'2025-07-01','2025-09-30',40),(19,'AI Ứng Dụng','Ứng dụng AI trong thực tế','/com/ntn/images/courses/img_19.jpg',7,1,200000,'2025-07-01','2025-09-30',40),(20,'AI Chuyên Sâu','Deep Learning nâng cao','/com/ntn/images/courses/img_20.jpg',7,1,200000,'2025-07-01','2025-09-30',40),(21,'React nâng cao','Học react nâng cao','/com/ntn/images/courses/img_21.jpg',4,1,500000,'2025-07-01','2025-09-30',40),(22,'Quản trị viên','Luyện tập quản trị mạng','/com/ntn/images/courses/img_22.jpg',3,1,700000,'2025-07-01','2025-09-30',40),(23,'Quản trị mạng','Luyện tập quản trị mạng','/com/ntn/images/courses/img_23.jpg',4,1,200000,'2025-07-01','2025-09-30',40),(24,'An toàn hệ thống thông tin','Luyện tập bảo mật','/com/ntn/images/courses/img_24.jpg',5,1,200000,'2025-07-01','2025-09-30',40),(25,'Kiểm thử phần mêm','Học về kỹ năng kiểm thử phần mềm','/com/ntn/images/courses/img_25.jpg',3,1,1000000,'2025-07-01','2025-09-30',40),(26,'Điện toán đám mây','Nâng cao về điện toán đám mây','/com/ntn/images/courses/course_26.jpg',7,1,1000000,'2025-04-21','2025-06-28',40);
/*!40000 ALTER TABLE `khoahoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `khoahoc_hocvien`
--

DROP TABLE IF EXISTS `khoahoc_hocvien`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `khoahoc_hocvien` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hocVienID` int NOT NULL,
  `khoaHocID` int NOT NULL,
  `ngay_dang_ky` datetime DEFAULT CURRENT_TIMESTAMP,
  `trang_thai` varchar(50) NOT NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  KEY `idx_khoahoc_hocvien_khoaHocID` (`khoaHocID`),
  KEY `idx_khoahoc_hocvien_hocVienID` (`hocVienID`),
  CONSTRAINT `khoahoc_hocvien_ibfk_1` FOREIGN KEY (`hocVienID`) REFERENCES `hocvien` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `khoahoc_hocvien_ibfk_2` FOREIGN KEY (`khoaHocID`) REFERENCES `khoahoc` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `khoahoc_hocvien`
--

LOCK TABLES `khoahoc_hocvien` WRITE;
/*!40000 ALTER TABLE `khoahoc_hocvien` DISABLE KEYS */;
INSERT INTO `khoahoc_hocvien` VALUES (1,1,1,'2025-04-15 10:00:00','ENROLLED'),(2,1,2,'2025-04-15 10:00:00','ENROLLED'),(3,24,3,'2025-04-15 10:00:00','ENROLLED'),(4,2,4,'2025-04-15 10:00:00','ENROLLED'),(5,3,5,'2025-04-15 10:00:00','ENROLLED'),(6,3,6,'2025-04-15 10:00:00','ENROLLED'),(7,4,7,'2025-04-15 10:00:00','ENROLLED'),(8,4,8,'2025-04-15 10:00:00','ENROLLED'),(9,5,9,'2025-04-15 10:00:00','ENROLLED'),(10,5,10,'2025-04-15 10:00:00','ENROLLED'),(11,6,11,'2025-04-15 10:00:00','ENROLLED'),(12,6,12,'2025-04-15 10:00:00','ENROLLED'),(13,7,13,'2025-04-15 10:00:00','ENROLLED'),(14,7,14,'2025-04-15 10:00:00','ENROLLED'),(15,8,15,'2025-04-15 10:00:00','ENROLLED'),(16,8,16,'2025-04-15 10:00:00','ENROLLED'),(17,9,17,'2025-04-15 10:00:00','ENROLLED'),(18,9,18,'2025-04-15 10:00:00','ENROLLED'),(19,10,19,'2025-04-15 10:00:00','ENROLLED'),(20,10,20,'2025-04-15 10:00:00','ENROLLED'),(21,11,3,'2025-04-17 00:00:00','ENROLLED'),(22,11,9,'2025-04-17 00:00:00','ENROLLED'),(23,10,18,'2025-04-17 00:00:00','ENROLLED'),(27,7,23,'2025-04-18 00:00:00','APPROVED'),(28,7,5,'2025-04-18 00:00:00','APPROVED'),(29,24,3,'2025-04-18 00:00:00','APPROVED'),(30,7,5,'2025-04-18 00:00:00','APPROVED'),(31,7,3,'2025-04-18 00:00:00','APPROVED'),(32,24,24,'2025-04-18 00:00:00','APPROVED'),(33,24,2,'2025-04-21 00:00:00','PENDING');
/*!40000 ALTER TABLE `khoahoc_hocvien` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lich_hoc`
--

DROP TABLE IF EXISTS `lich_hoc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lich_hoc` (
  `id` int NOT NULL AUTO_INCREMENT,
  `khoaHocId` int NOT NULL,
  `ngay_hoc` date NOT NULL,
  `gio_bat_dau` time NOT NULL,
  `gio_ket_thuc` time NOT NULL,
  `lien_ket` varchar(255) DEFAULT NULL,
  `giangVienId` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lich_hoc_khoaHocId` (`khoaHocId`),
  KEY `idx_lich_hoc_ngay_hoc` (`ngay_hoc`),
  KEY `idx_lich_hoc_giangVienId` (`giangVienId`),
  CONSTRAINT `lich_hoc_ibfk_1` FOREIGN KEY (`khoaHocId`) REFERENCES `khoahoc` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `lich_hoc_ibfk_2` FOREIGN KEY (`giangVienId`) REFERENCES `giangvien` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lich_hoc`
--

LOCK TABLES `lich_hoc` WRITE;
/*!40000 ALTER TABLE `lich_hoc` DISABLE KEYS */;
INSERT INTO `lich_hoc` VALUES (1,1,'2025-04-16','08:00:00','10:00:00','https://zoom.us/j/1234567890',6),(2,1,'2025-04-18','08:00:00','10:00:00','https://zoom.us/j/1234567891',6),(3,1,'2025-04-23','08:00:00','10:00:00','https://zoom.us/j/1234567892',6),(4,1,'2025-04-25','08:00:00','10:00:00','https://zoom.us/j/1234567893',6),(5,1,'2025-04-30','08:00:00','10:00:00','https://zoom.us/j/1234567894',6),(6,2,'2025-04-16','14:00:00','16:00:00','https://meet.google.com/abc-defg-hij',3),(7,2,'2025-04-18','14:00:00','16:00:00','https://meet.google.com/abc-defg-hik',3),(8,2,'2025-04-23','14:00:00','16:00:00','https://meet.google.com/abc-defg-hil',3),(9,8,'2025-04-17','09:00:00','11:00:00','https://zoom.us/j/9876543210',4),(10,8,'2025-04-19','09:00:00','11:00:00','https://zoom.us/j/9876543211',4),(11,8,'2025-04-24','09:00:00','11:00:00','https://zoom.us/j/9876543212',4),(12,1,'2025-04-16','08:00:00','10:00:00','https://zoom.us/j/1234567890',6),(13,1,'2025-04-18','08:00:00','10:00:00','https://zoom.us/j/1234567891',6),(14,1,'2025-04-23','08:00:00','10:00:00','https://zoom.us/j/1234567892',6),(15,1,'2025-04-25','08:00:00','10:00:00','https://zoom.us/j/1234567893',6),(16,1,'2025-04-30','08:00:00','10:00:00','https://zoom.us/j/1234567894',6),(17,2,'2025-04-16','14:00:00','16:00:00','https://meet.google.com/abc-defg-hij',3),(18,2,'2025-04-18','14:00:00','16:00:00','https://meet.google.com/abc-defg-hik',3),(19,2,'2025-04-23','14:00:00','16:00:00','https://meet.google.com/abc-defg-hil',3),(20,8,'2025-04-17','09:00:00','11:00:00','https://zoom.us/j/9876543210',4),(21,8,'2025-04-19','09:00:00','11:00:00','https://zoom.us/j/9876543211',4),(22,8,'2025-04-24','09:00:00','11:00:00','https://zoom.us/j/9876543212',4);
/*!40000 ALTER TABLE `lich_hoc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lichsu_thanhtoan`
--

DROP TABLE IF EXISTS `lichsu_thanhtoan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lichsu_thanhtoan` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hocVienID` int DEFAULT NULL,
  `khoaHocID` int DEFAULT NULL,
  `so_tien` decimal(10,2) NOT NULL,
  `ngay_thanh_toan` datetime DEFAULT CURRENT_TIMESTAMP,
  `phuong_thuc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `khoaHocID` (`khoaHocID`),
  KEY `lichsu_thanhtoan_ibfk_1` (`hocVienID`),
  CONSTRAINT `lichsu_thanhtoan_ibfk_1` FOREIGN KEY (`hocVienID`) REFERENCES `hocvien` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `lichsu_thanhtoan_ibfk_2` FOREIGN KEY (`khoaHocID`) REFERENCES `khoahoc` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lichsu_thanhtoan`
--

LOCK TABLES `lichsu_thanhtoan` WRITE;
/*!40000 ALTER TABLE `lichsu_thanhtoan` DISABLE KEYS */;
INSERT INTO `lichsu_thanhtoan` VALUES (2,1,1,200000.00,'2025-04-15 10:00:00','Chuyển khoản'),(3,2,2,150.00,'2025-04-15 10:00:00','Chuyển khoản'),(4,3,3,200.00,'2025-04-15 10:00:00','Chuyển khoản'),(5,1,4,250.00,'2025-04-15 10:00:00','Thẻ tín dụng'),(6,2,5,120.00,'2025-04-15 10:00:00','Chuyển khoản'),(21,1,1,200000.00,'2025-04-27 00:00:00','Chuyển khoản'),(22,4,3,200.00,'2025-07-17 00:00:00','Chuyển khoản'),(23,3,14,150.00,'2025-04-15 10:00:00','Chuyển khoản'),(24,2,20,400.00,'2025-04-26 00:00:00','Thẻ tín dụng'),(27,5,25,1000000.00,'2025-08-18 00:00:00','Chuyển khoản'),(28,4,1,200000.00,'2025-11-29 00:00:00','Chuyển khoản'),(29,6,13,100.00,'2025-10-02 00:00:00','Chuyển khoản'),(30,2,23,200000.00,'2025-07-26 00:00:00','Chuyển khoản'),(31,7,1,200000.00,'2025-07-21 00:00:00','Chuyển khoản'),(37,1,21,500000.00,'2025-11-20 00:00:00','Chuyển khoản'),(38,3,17,200.00,'2025-04-15 10:00:00','Chuyển khoản'),(39,3,4,250.00,'2025-04-15 10:00:00','Chuyển khoản'),(41,5,6,180.00,'2025-07-10 00:00:00','Chuyển khoản'),(42,7,3,200.00,'2025-11-04 00:00:00','Chuyển khoản'),(43,5,18,300.00,'2025-09-09 00:00:00','Chuyển khoản'),(44,5,1,200000.00,'2025-04-15 10:00:00','Chuyển khoản'),(45,4,18,300.00,'2025-07-21 00:00:00','Chuyển khoản'),(47,7,15,200.00,'2025-11-03 00:00:00','Chuyển khoản'),(49,7,3,200.00,'2025-12-21 00:00:00','Chuyển khoản'),(50,4,17,200.00,'2025-04-18 00:00:00','Chuyển khoản'),(51,4,9,200.00,'2025-05-11 00:00:00','Chuyển khoản'),(52,6,22,700000.00,'2025-07-31 00:00:00','Chuyển khoản'),(53,7,7,220.00,'2025-04-15 10:00:00','Chuyển khoản'),(54,6,11,300.00,'2025-09-04 00:00:00','Chuyển khoản'),(56,4,23,200000.00,'2025-04-15 10:00:00','Chuyển khoản'),(57,3,8,300.00,'2025-12-19 00:00:00','Chuyển khoản'),(60,3,16,250.00,'2025-04-15 10:00:00','Chuyển khoản'),(61,4,15,200.00,'2025-09-25 00:00:00','Chuyển khoản'),(63,3,17,200.00,'2025-06-01 00:00:00','Chuyển khoản'),(65,3,23,200000.00,'2025-11-14 00:00:00','Chuyển khoản'),(66,6,1,200000.00,'2025-06-05 00:00:00','Chuyển khoản'),(68,1,19,350.00,'2025-05-25 00:00:00','Chuyển khoản'),(69,2,25,1000000.00,'2025-10-03 00:00:00','Chuyển khoản'),(70,5,15,200.00,'2025-07-19 00:00:00','Chuyển khoản'),(72,4,23,200000.00,'2025-11-10 00:00:00','Chuyển khoản'),(73,5,9,200.00,'2025-11-08 00:00:00','Chuyển khoản'),(74,2,12,350.00,'2025-12-16 00:00:00','Chuyển khoản'),(76,1,13,100.00,'2025-08-25 00:00:00','Chuyển khoản'),(77,3,5,120.00,'2025-07-24 00:00:00','Chuyển khoản'),(80,1,22,700000.00,'2025-10-20 00:00:00','Chuyển khoản'),(81,5,22,700000.00,'2025-10-18 00:00:00','Chuyển khoản'),(82,4,20,400.00,'2025-12-17 00:00:00','Chuyển khoản'),(83,2,1,200000.00,'2025-04-15 10:00:00','Chuyển khoản'),(85,6,16,250.00,'2025-08-16 00:00:00','Chuyển khoản'),(86,6,23,200000.00,'2025-04-15 10:00:00','Chuyển khoản'),(87,1,20,400.00,'2025-04-15 10:00:00','Chuyển khoản'),(88,4,5,120.00,'2025-04-15 10:00:00','Chuyển khoản'),(89,7,17,200.00,'2025-04-15 10:00:00','Chuyển khoản'),(90,5,3,200.00,'2025-12-01 00:00:00','Chuyển khoản'),(91,6,12,350.00,'2025-07-14 00:00:00','Chuyển khoản'),(92,3,6,180.00,'2025-05-29 00:00:00','Chuyển khoản'),(93,1,13,100.00,'2025-07-22 00:00:00','Chuyển khoản'),(94,3,5,120.00,'2025-12-08 00:00:00','Chuyển khoản'),(96,4,1,200000.00,'2025-04-19 00:00:00','Chuyển khoản'),(98,5,19,350.00,'2025-04-15 10:00:00','Chuyển khoản'),(99,1,1,200000.00,'2025-04-13 00:00:00','Chuyển khoản'),(100,5,8,300.00,'2025-04-13 00:00:00','Chuyển khoản'),(101,4,3,200.00,'2025-04-17 00:00:00','Chuyển khoản'),(102,4,9,200.00,'2025-04-17 00:00:00','Chuyển khoản'),(103,3,18,300.00,'2025-04-17 00:00:00','Chuyển khoản'),(104,6,19,350.00,'2025-04-17 00:00:00','Chuyển khoản'),(105,6,19,350.00,'2025-04-17 00:00:00','Chuyển khoản'),(106,6,3,200.00,'2025-04-18 00:00:00','Chuyển khoản'),(107,7,23,200000.00,'2025-04-18 00:00:00','Chuyển khoản'),(108,7,5,120.00,'2025-04-18 00:00:00','Chuyển khoản'),(109,7,3,200.00,'2025-04-18 00:00:00','Chuyển khoản'),(110,7,5,120.00,'2025-04-18 00:00:00','Chuyển khoản'),(111,7,3,200.00,'2025-04-18 00:00:00','Chuyển khoản'),(112,7,24,200000.00,'2025-04-18 00:00:00','Chuyển khoản'),(113,20,2,1000000.00,'2025-04-19 00:00:00','Tiền mặt'),(114,24,2,650000.00,'2025-04-21 00:00:00','Chuyển khoản');
/*!40000 ALTER TABLE `lichsu_thanhtoan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loainguoidung`
--

DROP TABLE IF EXISTS `loainguoidung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loainguoidung` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ten_loai` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loainguoidung`
--

LOCK TABLES `loainguoidung` WRITE;
/*!40000 ALTER TABLE `loainguoidung` DISABLE KEYS */;
INSERT INTO `loainguoidung` VALUES (1,'Quản trị viên'),(2,'Giảng viên'),(3,'Học viên');
/*!40000 ALTER TABLE `loainguoidung` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nguoidung`
--

DROP TABLE IF EXISTS `nguoidung`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nguoidung` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ho` varchar(50) NOT NULL,
  `ten` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mat_khau` varchar(255) NOT NULL,
  `loai_nguoi_dung_id` int NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `so_lan_thu_dang_nhap_sai` int DEFAULT '0',
  `thoi_gian_khoa_tai_khoan` timestamp NULL DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `loai_nguoi_dung_id` (`loai_nguoi_dung_id`),
  KEY `idx_nguoidung_email` (`email`),
  CONSTRAINT `nguoidung_ibfk_1` FOREIGN KEY (`loai_nguoi_dung_id`) REFERENCES `loainguoidung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nguoidung`
--

LOCK TABLES `nguoidung` WRITE;
/*!40000 ALTER TABLE `nguoidung` DISABLE KEYS */;
INSERT INTO `nguoidung` VALUES (1,'Nguyễn','Admin Một','admin1@example.com','$2a$10$P10DWWNYYZIalkQcmMRD4O/USKRGkmf/tCuY5ukXzOgrdcVAHucfS',1,1,0,NULL,'/com/ntn/images/avatars/1_1744429268003.jpg'),(2,'Trần','Admin Hai','admin2@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',1,1,1,NULL,'/com/ntn/images/avatars/2_1744917402788.jpg'),(3,'Nguyễn','Văn A','gv1@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',2,1,0,NULL,'/com/ntn/images/avatars/3_1744917415160.jpg'),(4,'Trần','Thị B','gv2@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',2,1,0,NULL,'/com/ntn/images/avatars/4_1744917428234.jpg'),(5,'Lê','Văn C','gv3@example.com','Nhat#1908',2,1,0,NULL,'/com/ntn/images/avatars/5_1744349692304.jpg'),(6,'Phạm','Thị D','gv4@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',2,1,0,NULL,'/com/ntn/images/avatars/6_1744349964884.jpg'),(7,'Nguyễn','Văn E','gv5@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',2,1,0,NULL,'E:\\OneDrive\\Pictures\\Screenshots\\Screenshot 2025-04-09 104543.png'),(8,'Nguyễn','Văn F','hv1@example.com','$2a$10$55mC1CB3bSq/BbmBcP7sse3HHZQMFB.vXEZO.6FvvyxuL/5VpTQa2',3,1,0,NULL,'default.jpg'),(9,'Trần','Thị G','hv2@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(10,'Lê','Văn H','hv3@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(11,'Phạm','Thị I','hv4@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(12,'Nguyễn','Văn J','hv5@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(13,'Trần','Văn K','hv6@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(14,'Lê','Thị L','hv7@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'/com/ntn/images/avatars/14_1744558922373.jpg'),(15,'Phạm','Văn M','hv8@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(16,'Nguyễn','Thị N','hv9@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(17,'Trần','Văn O','hv10@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(18,'Lê','Thị P','hv11@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(19,'Phạm','Văn Q','hv12@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(20,'Nguyễn','Thị R','hv13@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(21,'Trần','Văn S','hv14@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(22,'Lê','Thị T','hv15@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(23,'Phạm','Văn U','hv16@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(24,'Nguyễn','Thị V','hv17@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(25,'Trần','Văn W','hv18@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(26,'Lê','Thị X','hv19@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(27,'Phạm','Văn Y','hv20@example.com','$2a$10$sg7FS/vFQBKavaQ1egIIROfdJJ0XmY2qR8.36cshH51mZRuFZViV.',3,1,0,NULL,'default.jpg'),(28,'Nguyễn Thành','Nhật','2251052082nhat@ou.edu.vn','$2a$10$cmtCzx6qXVp8nsFu6cnv/ei7vJC/8mZdGT4q8sGlffbYkGnlyvn6a',3,1,0,NULL,'default.jpg'),(29,'Võ','Tú','tuvo12@gmail.com','$2a$10$hksL0WRlWyqrFPBi1aHz2uXgj1LBovRZeas4Zq0DCDY.cSGf8nwj2',3,1,0,NULL,'/com/ntn/images/avatars/29_1744429295703.jpg'),(30,'Lê','Nguyện','nguyenle12@gmail.com','$2a$10$fMJ04TAjCU/P7Fr0CS3cnOgzfn8GJLTtLeP8S176RPBHw3HUbGTwm',3,1,0,NULL,'/com/ntn/images/avatars/30_1744387635986.jpg'),(34,'Nhật','Nguyễn','nhat12@gmail.com','$2a$10$RahZH8qSOPE1ZjjmWgFIDu7w/AGEuKdkdWt.OWNopw1n1Y6M1rbO2',3,1,0,NULL,'/com/ntn/images/avatars/34_1744384192248.jpg'),(35,'Lê Văn','Toàn','vantoan1@gmail.com','$2a$10$Sl0dQJCHGZri1pNIOvnA0Oz2nJ2W8e1jNtdBqE5uljglTjFRVWjFS',3,1,0,NULL,'33.jpg'),(37,'Nguyễn','Thành','nt1@gmail.com','$2a$10$TrpkfMtXBfzelPxNM4pJwOvjp37Qy2AlcxEU18YcsQfrqJBhWLhT2',3,1,0,NULL,'/com/ntn/images/avatars/33.jpg'),(38,'Nguyễn','Phi','np1@gmail.com','$2a$10$pQ99mP0gGsUfDKX.EgIRfecmENT68C29EtjFdYPQ/i5EyumhflecO',3,1,0,NULL,'/com/ntn/images/avatars/34.jpg'),(39,'le','A','NguyenVanD@gmail.com','$2a$10$8ym1i3IlKM/hYaP0t5cEqu/85D7/9zQIM07WtpeiVwkpBBxbCzqSe',3,1,0,NULL,NULL);
/*!40000 ALTER TABLE `nguoidung` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thongbao`
--

DROP TABLE IF EXISTS `thongbao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thongbao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `noi_dung` varchar(255) NOT NULL,
  `nguoi_nhan_id` int NOT NULL,
  `ngay_gui` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `trang_thai` varchar(50) NOT NULL DEFAULT 'UNREAD',
  PRIMARY KEY (`id`),
  KEY `nguoi_nhan_id` (`nguoi_nhan_id`),
  CONSTRAINT `thongbao_ibfk_1` FOREIGN KEY (`nguoi_nhan_id`) REFERENCES `nguoidung` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thongbao`
--

LOCK TABLES `thongbao` WRITE;
/*!40000 ALTER TABLE `thongbao` DISABLE KEYS */;
INSERT INTO `thongbao` VALUES (1,'Học viên ID=11 đã thanh toán cho khóa học ID=3 (ThanhToanID=44). Vui lòng xét duyệt.',1,'2025-04-17 03:33:11','UNREAD'),(2,'Yêu cầu tham gia khóa học ID=3 của bạn đã được duyệt.',11,'2025-04-17 03:39:04','READ'),(3,'Học viên ID=11 đã thanh toán cho khóa học ID=9 (ThanhToanID=45). Vui lòng xét duyệt.',1,'2025-04-17 03:40:19','UNREAD'),(4,'Yêu cầu tham gia khóa học ID=9 của bạn đã được duyệt.',11,'2025-04-17 03:40:39','READ'),(5,'Học viên ID=10 đã thanh toán cho khóa học ID=18 (ThanhToanID=46). Vui lòng xét duyệt.',1,'2025-04-17 03:50:02','UNREAD'),(6,'Yêu cầu tham gia khóa học ID=18 của bạn đã được duyệt.',10,'2025-04-17 03:50:29','READ'),(7,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: Quản trị mạng',1,'2025-04-18 00:04:27','UNREAD'),(8,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: Python Cơ Bản',1,'2025-04-18 00:22:20','UNREAD'),(9,'Yêu cầu tham gia khóa học ID=23 của bạn đã được duyệt.',7,'2025-04-18 00:26:53','UNREAD'),(10,'Yêu cầu tham gia khóa học ID=5 của bạn đã được duyệt.',7,'2025-04-18 00:26:55','UNREAD'),(11,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: Java Web',1,'2025-04-18 00:27:26','UNREAD'),(12,'Yêu cầu tham gia khóa học ID=3 của bạn đã được duyệt.',7,'2025-04-18 00:27:52','UNREAD'),(13,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: Python Cơ Bản',1,'2025-04-18 00:44:01','UNREAD'),(14,'Yêu cầu tham gia khóa học ID=5 của bạn đã được duyệt.',7,'2025-04-18 00:44:25','UNREAD'),(15,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: Java Web',1,'2025-04-18 00:44:54','UNREAD'),(16,'Yêu cầu tham gia khóa học ID=3 của bạn đã được duyệt.',7,'2025-04-18 00:45:34','UNREAD'),(17,'Học viên Lê Thị L yêu cầu duyệt đăng ký khóa học: An toàn hệ thống thông tin',1,'2025-04-18 01:01:24','UNREAD'),(18,'Yêu cầu tham gia khóa học ID=24 của bạn đã được duyệt.',7,'2025-04-18 01:02:45','UNREAD'),(19,'Học viên le A yêu cầu duyệt đăng ký khóa học: Java Nâng Cao',1,'2025-04-21 16:54:12','UNREAD');
/*!40000 ALTER TABLE `thongbao` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-21 20:53:04
