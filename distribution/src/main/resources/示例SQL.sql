-- database: jfinal_demo
-- use jfinal_demo;

CREATE TABLE `blog` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` mediumtext NOT NULL COMMENT '内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='博客';


INSERT INTO `blog` VALUES
('1', 'test 0', 'test 0 Content'),
('2', 'test 1', 'test 1'),
('3', 'test 2', 'test 2'),
('4', 'test 3', 'test 3'),
('5', 'test 4', 'test 4');
