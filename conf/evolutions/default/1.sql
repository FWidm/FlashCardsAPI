# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table message (
  messagetype               varchar(31) not null,
  messageId                 bigint auto_increment not null,
  recipient                 bigint,
  content                   varchar(255),
  created                   datetime(6) not null,
  targetDeck                bigint,
  constraint pk_message primary key (messageId))
;

create table answer (
  answerId                  bigint auto_increment not null,
  answerText                varchar(255),
  answerHint                varchar(255),
  mediaURI                  varchar(255),
  userId                    bigint,
  cardId                    bigint,
  rating                    integer,
  answerCorrect             tinyint(1) default 0,
  created                   datetime(6) not null,
  lastUpdated               datetime(6) not null,
  constraint pk_answer primary key (answerId))
;

create table authToken (
  tokenId                   bigint auto_increment not null,
  tokenUserId               bigint,
  token                     varchar(255),
  created                   datetime(6) not null,
  constraint uq_authToken_token unique (token),
  constraint pk_authToken primary key (tokenId))
;

create table cardDeck (
  cardDeckId                bigint auto_increment not null,
  visible                   tinyint(1) default 0,
  userGroup                 bigint,
  cardDeckName              varchar(255),
  description               varchar(255),
  category                  bigint,
  constraint pk_cardDeck primary key (cardDeckId))
;

create table category (
  categoryId                bigint auto_increment not null,
  categoryName              varchar(255),
  parent_categoryId         bigint,
  constraint pk_category primary key (categoryId))
;

create table flashCard (
  flashcardId               bigint auto_increment not null,
  rating                    integer,
  questionId                bigint,
  userId                    bigint,
  cardDeckId                bigint,
  multipleChoice            tinyint(1) default 0,
  created                   datetime(6) not null,
  lastUpdated               datetime(6) not null,
  constraint uq_flashCard_questionId unique (questionId),
  constraint pk_flashCard primary key (flashcardId))
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
  answerId                  bigint,
  flashcardId               bigint,
  constraint uq_rating_1 unique (userId,answerId,flashcardId),
  constraint pk_rating primary key (ratingId))
;

create table statistics (
  statisticId               bigint auto_increment not null,
  user                      bigint,
  card                      bigint,
  knowledge                 float,
  drawer                    integer,
  startDate                 datetime(6),
  endDate                   datetime(6),
  constraint pk_statistics primary key (statisticId))
;

create table tag (
  tagId                     bigint auto_increment not null,
  tagName                   varchar(255),
  constraint uq_tag_tagName unique (tagName),
  constraint pk_tag primary key (tagId))
;

create table uploaded_media (
  mediaId                   bigint auto_increment not null,
  mediaURI                  varchar(255),
  userId                    bigint,
  media_type                varchar(255),
  created                   datetime(6) not null,
  constraint pk_uploaded_media primary key (mediaId))
;

create table user (
  userId                    bigint auto_increment not null,
  avatar                    longtext,
  name                      varchar(255),
  password                  varchar(255),
  email                     varchar(255),
  rating                    integer,
  created                   datetime(6) not null,
  lastLogin                 datetime(6) not null,
  constraint uq_user_email unique (email),
  constraint pk_user primary key (userId))
;

create table userGroup (
  groupId                   bigint auto_increment not null,
  name                      varchar(255),
  description               varchar(255),
  constraint pk_userGroup primary key (groupId))
;


create table cardTagJoinTable (
  flashcardId                    bigint not null,
  tagId                          bigint not null,
  constraint pk_cardTagJoinTable primary key (flashcardId, tagId))
;

create table userGroupJoinTable (
  userId                         bigint not null,
  groupId                        bigint not null,
  constraint pk_userGroupJoinTable primary key (userId, groupId))
;
alter table message add constraint fk_message_recipient_1 foreign key (recipient) references user (userId) on delete restrict on update restrict;
create index ix_message_recipient_1 on message (recipient);
alter table message add constraint fk_message_deck_2 foreign key (targetDeck) references cardDeck (cardDeckId) on delete restrict on update restrict;
create index ix_message_deck_2 on message (targetDeck);
alter table answer add constraint fk_answer_author_3 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_answer_author_3 on answer (userId);
alter table answer add constraint fk_answer_card_4 foreign key (cardId) references flashCard (flashcardId) on delete restrict on update restrict;
create index ix_answer_card_4 on answer (cardId);
alter table authToken add constraint fk_authToken_user_5 foreign key (tokenUserId) references user (userId) on delete restrict on update restrict;
create index ix_authToken_user_5 on authToken (tokenUserId);
alter table cardDeck add constraint fk_cardDeck_userGroup_6 foreign key (userGroup) references userGroup (groupId) on delete restrict on update restrict;
create index ix_cardDeck_userGroup_6 on cardDeck (userGroup);
alter table cardDeck add constraint fk_cardDeck_category_7 foreign key (category) references category (categoryId) on delete restrict on update restrict;
create index ix_cardDeck_category_7 on cardDeck (category);
alter table category add constraint fk_category_parent_8 foreign key (parent_categoryId) references category (categoryId) on delete restrict on update restrict;
create index ix_category_parent_8 on category (parent_categoryId);
alter table flashCard add constraint fk_flashCard_question_9 foreign key (questionId) references question (questionId) on delete restrict on update restrict;
create index ix_flashCard_question_9 on flashCard (questionId);
alter table flashCard add constraint fk_flashCard_author_10 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_flashCard_author_10 on flashCard (userId);
alter table flashCard add constraint fk_flashCard_deck_11 foreign key (cardDeckId) references cardDeck (cardDeckId) on delete restrict on update restrict;
create index ix_flashCard_deck_11 on flashCard (cardDeckId);
alter table question add constraint fk_question_author_12 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_question_author_12 on question (userId);
alter table rating add constraint fk_rating_author_13 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_rating_author_13 on rating (userId);
alter table rating add constraint fk_rating_ratedAnswer_14 foreign key (answerId) references answer (answerId) on delete restrict on update restrict;
create index ix_rating_ratedAnswer_14 on rating (answerId);
alter table rating add constraint fk_rating_ratedFlashCard_15 foreign key (flashcardId) references flashCard (flashcardId) on delete restrict on update restrict;
create index ix_rating_ratedFlashCard_15 on rating (flashcardId);
alter table statistics add constraint fk_statistics_user_16 foreign key (user) references user (userId) on delete restrict on update restrict;
create index ix_statistics_user_16 on statistics (user);
alter table statistics add constraint fk_statistics_card_17 foreign key (card) references flashCard (flashcardId) on delete restrict on update restrict;
create index ix_statistics_card_17 on statistics (card);
alter table uploaded_media add constraint fk_uploaded_media_author_18 foreign key (userId) references user (userId) on delete restrict on update restrict;
create index ix_uploaded_media_author_18 on uploaded_media (userId);



alter table cardTagJoinTable add constraint fk_cardTagJoinTable_flashCard_01 foreign key (flashcardId) references flashCard (flashcardId) on delete restrict on update restrict;

alter table cardTagJoinTable add constraint fk_cardTagJoinTable_tag_02 foreign key (tagId) references tag (tagId) on delete restrict on update restrict;

alter table userGroupJoinTable add constraint fk_userGroupJoinTable_user_01 foreign key (userId) references user (userId) on delete restrict on update restrict;

alter table userGroupJoinTable add constraint fk_userGroupJoinTable_userGroup_02 foreign key (groupId) references userGroup (groupId) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table message;

drop table answer;

drop table authToken;

drop table cardDeck;

drop table category;

drop table flashCard;

drop table cardTagJoinTable;

drop table question;

drop table rating;

drop table statistics;

drop table tag;

drop table uploaded_media;

drop table user;

drop table userGroupJoinTable;

drop table userGroup;

SET FOREIGN_KEY_CHECKS=1;

