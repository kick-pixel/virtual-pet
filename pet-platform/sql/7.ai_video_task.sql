ALTER TABLE ai_video_task 
  ADD COLUMN video_pic_file_id BIGINT DEFAULT NULL COMMENT '视频首帧图片文件ID',
  ADD COLUMN video_pic_url VARCHAR(500) DEFAULT NULL COMMENT '视频首帧图片URL';
