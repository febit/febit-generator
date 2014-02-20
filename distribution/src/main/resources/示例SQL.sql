-- database: jfinal_demo
-- use jfinal_demo;

CREATE TABLE `blog` (
  `id` int(11) NOT NULL auto_increment,
  `title` varchar(200) NOT NULL,
  `content` mediumtext NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='博客';


INSERT INTO `blog` VALUES
('1', 'test 0', 'test 0 Content'),
('2', 'test 1', 'test 1'),
('3', 'test 2', 'test 2'),
('4', 'test 3', 'test 3'),
('5', 'test 4', 'test 4');