INSERT INTO stocks (stock_code, stock_name, market_type, logo_url) VALUES
                                                                       ('005930', '삼성전자', 'KOR', 'https://logo.clearbit.com/samsung.com'),
                                                                       ('000660', 'SK하이닉스', 'KOR', 'https://logo.clearbit.com/skhynix.com'),
                                                                       ('207940', '삼성바이오로직스', 'KOR', 'https://logo.clearbit.com/samsungbiologics.com'),
                                                                       ('373220', 'LG에너지솔루션', 'KOR', 'https://logo.clearbit.com/lgensol.com'),
                                                                       ('012450', '한화에어로스페이스', 'KOR', 'https://logo.clearbit.com/hanwhaaerospace.co.kr'),
                                                                       ('105560', 'KB금융', 'KOR', 'https://logo.clearbit.com/kbfg.com'),
                                                                       ('005380', '현대차', 'KOR', 'https://logo.clearbit.com/hyundai.com'),
                                                                       ('005935', '삼성전자우', 'KOR', 'https://logo.clearbit.com/samsung.com'),
                                                                       ('329180', 'HD현대중공업', 'KOR', 'https://www.hhi.co.kr/images/common/pclogo_new.png'),
                                                                       ('000270', '기아', 'KOR', 'https://logo.clearbit.com/kia.com'),
                                                                       ('068270', '셀트리온', 'KOR', 'https://logo.clearbit.com/celltrion.com'),
                                                                       ('035420', 'NAVER', 'KOR', 'https://logo.clearbit.com/navercorp.com'),
                                                                       ('034020', '두산에너빌리티', 'KOR', 'https://logo.clearbit.com/doosanenerbility.com'),
                                                                       ('055550', '신한지주', 'KOR', 'https://logo.clearbit.com/shinhangroup.com'),
                                                                       ('028260', '삼성물산', 'KOR', 'https://logo.clearbit.com/samsungcnt.com'),
                                                                       ('012330', '현대모비스', 'KOR', 'data:image/png;base64,iVBORw0K...'),
                                                                       ('042660', '한화오션', 'KOR', 'https://logo.clearbit.com/hanwha.com'),
                                                                       ('009540', 'HD한국조선해양', 'KOR', 'https://logo.clearbit.com/ksoe.co.kr'),
                                                                       ('032830', '삼성생명', 'KOR', 'https://logo.clearbit.com/samsunglife.com'),
                                                                       ('011200', 'HMM', 'KOR', 'data:image/png;base64,iVBORw0K...'),
                                                                       ('086790', '하나금융지주', 'KOR', 'https://logo.clearbit.com/hanafn.com'),
                                                                       ('035720', '카카오', 'KOR', 'https://logo.clearbit.com/kakaocorp.com'),
                                                                       ('005490', 'POSCO홀딩스', 'KOR', 'https://logo.clearbit.com/posco-inc.com'),
                                                                       ('138040', '메리츠금융지주', 'KOR', 'https://logo.clearbit.com/meritz.co.kr'),
                                                                       ('000810', '삼성화재', 'KOR', 'https://logo.clearbit.com/samsungfire.com'),
                                                                       ('064350', '현대로템', 'KOR', 'https://logo.clearbit.com/hyundai-rotem.co.kr'),
                                                                       ('259960', '크래프톤', 'KOR', 'https://logo.clearbit.com/krafton.com'),
                                                                       ('015760', '한국전력', 'KOR', 'https://logo.clearbit.com/kepco.co.kr'),
                                                                       ('402340', 'SK스퀘어', 'KOR', 'https://www.sksquare.com/assets/images/common/logo_lg.svg'),
                                                                       ('010130', '고려아연', 'KOR', 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ…');

UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/kia.png?alt=media&token=342bc920-9c46-444f-8d0e-1a04936a7380' WHERE stock_code = '000270';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/skhynix.png?alt=media&token=7ffb000f-c830-4fd2-866b-dc1fa23bbc11' WHERE stock_code = '000660';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsungfire.png?alt=media&token=e455abb1-18a9-4d89-8152-cd8e9568ae8d' WHERE stock_code = '000810';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/hyundai.png?alt=media&token=3f233e57-63e9-42ae-a269-ecadfbd8bd13' WHERE stock_code = '005380';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/posco-inc.png?alt=media&token=c8415519-f6d3-4796-99fa-ee78293a22af' WHERE stock_code = '005490';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsung.png?alt=media&token=f8e89ebd-e5bd-4352-ab99-6cf587a3fb09' WHERE stock_code = '005930';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsung(%E1%84%8B%E1%85%AE)png.png?alt=media&token=f17d0ec2-b1ff-471e-999d-3e9982918913' WHERE stock_code = '005935';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/ksoe.co.png?alt=media&token=0755f905-097a-40b7-9986-6ed7c3ad6693' WHERE stock_code = '009540';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/koruaa.jpg?alt=media&token=13cf4705-8fae-41f5-a69f-a3fd3787ac66' WHERE stock_code = '010130';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/HMM.png?alt=media&token=47b47088-0b6f-4721-b3fd-fa12e0c6af08' WHERE stock_code = '011200';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/%E1%84%92%E1%85%A7%E1%86%AB%E1%84%83%E1%85%A2%E1%84%86%E1%85%A9%E1%84%87%E1%85%B5%E1%84%89%E1%85%B3.png?alt=media&token=5b7f7f49-3c64-44f3-821c-094e7ea7655c' WHERE stock_code = '012330';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/hanwhaaerospace.co.png?alt=media&token=adff91b5-7a08-4ad1-897f-924d2a7dbe5c' WHERE stock_code = '012450';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/kepco.co.png?alt=media&token=605b86f1-1a93-4306-a52a-b27a27c06eb8' WHERE stock_code = '015760';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsungcnt.png?alt=media&token=3e801d87-53b4-4230-a5c4-3f28d1d8702d' WHERE stock_code = '028260';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsunglife.png?alt=media&token=342308f5-aced-4bac-84ca-f773215a7f8a' WHERE stock_code = '032830';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/doosanenerbility.png?alt=media&token=11a2cd9d-0ed3-422c-9ebc-94a04d43d6e9' WHERE stock_code = '034020';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/navercorp.png?alt=media&token=fde5c97a-ffcc-4a43-961d-c8b8c135e652' WHERE stock_code = '035420';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/kakaocorp.png?alt=media&token=83f24564-9a1b-4df6-a052-59b5352cb9c9' WHERE stock_code = '035720';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/hanwha.png?alt=media&token=484d89a7-a1a6-4ebc-b7c0-0184b42c85e8' WHERE stock_code = '042660';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/shinhangroup.png?alt=media&token=9f929726-4afc-4de5-b6c2-5652c96e8aba' WHERE stock_code = '055550';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/hyundai-rotem.co.png?alt=media&token=556bcb54-6da3-49dc-9ddf-0c3561746d23' WHERE stock_code = '064350';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/celltrion.png?alt=media&token=001a08ea-ee4f-46e5-bf3f-daf5b5a4dba9' WHERE stock_code = '068270';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/hanafn.png?alt=media&token=68c1db81-9c7b-4289-a081-054fa59a19fa' WHERE stock_code = '086790';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/kbfg.png?alt=media&token=7a5aa9ee-c685-4b5a-98c1-cac7c603f494' WHERE stock_code = '105560';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/meritz.co.png?alt=media&token=cbaea02f-4de0-4074-b74d-ad1c7bb310a8' WHERE stock_code = '138040';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/samsungbiologics.png?alt=media&token=fab8c192-37e2-44e5-b34e-424093c7330d' WHERE stock_code = '207940';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/krafton.png?alt=media&token=7be6662b-b7ed-402d-a02d-54d47a037cc4' WHERE stock_code = '259960';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/pclogo_new.png?alt=media&token=f0207e49-0bbb-47de-ac4a-04ba3e263d13' WHERE stock_code = '329180';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/lgensol.png?alt=media&token=1e342899-2f44-4d57-91f1-a6007312415d' WHERE stock_code = '373220';
UPDATE stocks SET logo_url = 'https://firebasestorage.googleapis.com/v0/b/it-s-okay-to-lose-127d2.firebasestorage.app/o/skSQUARE.png?alt=media&token=7c56a5e2-0d77-4978-8409-13ef87576dda' WHERE stock_code = '402340';
