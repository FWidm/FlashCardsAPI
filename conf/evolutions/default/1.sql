# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table answer (
  answerId                  bigint auto_increment not null,
  answerText                varchar(255),
  answerHint                varchar(255),
  mediaURI                  varchar(255),
  userId                    bigint,
  parent_card_id            bigint,
  rating                    integer,
  answerCorrect             tinyint(1) default 0,
  created                   datetime(6) not null,
  lastUpdated               datetime(6) not null,
  constraint pk_answer primary key (answerId))
;

create table auth_token (
  tokenId                   bigint auto_increment not null,
  userId                    bigint,
  token                     varchar(255),
  created                   datetime(6) not null,
  constraint uq_auth_token_token unique (token),
  constraint pk_auth_token primary key (tokenId))
;

create table flash_card (
  flashcardId               bigint auto_increment not null,
  rating                    integer,
  questionId                bigint,
  userId                    bigint,
  multipleChoice            tinyint(1) default 0,
  created                   datetime(6) not null,
  lastUpdated               datetime(6) not null,
  constraint uq_flash_card_questionId unique (questionId),
  constraint pk_flash_card primary key (flashcardId))
;

create table question (
  questionId                bigint auto_increment not null,
  questionText              varchar(255),
  mediaURI                  varchar(255),
  userId                    bigint,
  constraint pk_question primary key (questionId))
;

create table rating (
  ratingtype                varchar(31) not null,
  ratingId                  bigint auto_increment not null,
  userId                    bigint,
  ratingModifier            integer,
  flashcardId               bigint,
  answerId                  bigint,
  constraint uq_rating_1 unique (userId,answerId,flashcardId),
  constraint pk_rating primary key (ratingId))
;

create table tag (
  tagId                     bigint auto_increment not null,
  tagName                   varchar(255),
  constraint uq_tag_tagName unique (tagName),
  constraint pk_tag primary key (tagId))
;

create table user (
  userId                    bigint auto_increment not null,
  avatar                    longtext,
  name                      varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  rating                    integer,
  groupId                   bigint,
  created                   datetime(6) not null,
  lastLogin                 datetime(6) not null,
  constraint uq_user_email unique (email),
  constraint pk_user primary key (userId))
;

create table user_group (
  groupId                   bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_user_group primary key (groupId))
;


create table card_tag (
  flashcardId                    bigint not null,
  tagId                          bigint not null,
  constraint pk_card_tag primary key (flashcardId, tagId))
;
alter table answer add constraint fk_answer_author_1 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_answer_author_1 on answer (userId);
alter table answer add constraint fk_answer_card_2 foreign key (parent_card_id) references flash_card (flashcardId) on delete restrict on update restrict;
create index ix_answer_card_2 on answer (parent_card_id);
alter table auth_token add constraint fk_auth_token_user_3 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_auth_token_user_3 on auth_token (userId);
alter table flash_card add constraint fk_flash_card_question_4 foreign key (questionId) references question (questionId) on delete restrict on update restrict;
create index ix_flash_card_question_4 on flash_card (questionId);
alter table flash_card add constraint fk_flash_card_author_5 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_flash_card_author_5 on flash_card (userId);
alter table question add constraint fk_question_author_6 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_question_author_6 on question (userId);
alter table rating add constraint fk_rating_author_7 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_rating_author_7 on rating (userId);
alter table rating add constraint fk_rating_ratedFlashCard_8 foreign key (flashcardId) references flash_card (flashcardId) on delete restrict on update restrict;
create index ix_rating_ratedFlashCard_8 on rating (flashcardId);
alter table rating add constraint fk_rating_ratedAnswer_9 foreign key (answerId) references answer (answerId) on delete restrict on update restrict;
create index ix_rating_ratedAnswer_9 on rating (answerId);
alter table user add constraint fk_user_group_10 foreign key (groupId) references user_group (groupId) on delete restrict on update restrict;
create index ix_user_group_10 on user (groupId);



alter table card_tag add constraint fk_card_tag_flash_card_01 foreign key (flashcardId) references flash_card (flashcardId) on delete restrict on update restrict;

alter table card_tag add constraint fk_card_tag_tag_02 foreign key (tagId) references tag (tagId) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table answer;

drop table auth_token;

drop table flash_card;

drop table card_tag;

drop table question;

drop table rating;

drop table tag;

drop table user;

drop table user_group;

SET FOREIGN_KEY_CHECKS=1;

