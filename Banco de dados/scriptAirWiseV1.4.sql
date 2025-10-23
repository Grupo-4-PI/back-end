USE db_airwise;

CREATE TABLE empresa (
    idEmpresa INT AUTO_INCREMENT PRIMARY KEY,
    cnpj CHAR(14) NOT NULL,
    nomeFantasia VARCHAR(45) NOT NULL,
    razaoSocial VARCHAR(100) NOT NULL
);

CREATE TABLE usuario (
    idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    fkEmpresa INT NOT NULL,
    nome VARCHAR(45) NOT NULL,
    cpf CHAR(11) NOT NULL,
    email VARCHAR(45) NOT NULL,
    senha VARCHAR(45) NOT NULL,
    cargo VARCHAR(45),
    FOREIGN KEY (fkEmpresa) REFERENCES empresa(idEmpresa)
);

CREATE TABLE endereco (
    idEndereco INT AUTO_INCREMENT PRIMARY KEY,
    fkEmpresa INT NOT NULL,
    cep CHAR(9) NOT NULL,
    logradouro VARCHAR(45) NOT NULL,
    numero INT NOT NULL,
    bairro VARCHAR(45) NOT NULL,
    cidade VARCHAR(45) NOT NULL,
    uf CHAR(2) NOT NULL,
    FOREIGN KEY (fkEmpresa) REFERENCES empresa(idEmpresa)
);


CREATE TABLE chaveDeAcesso (
    idChave INT AUTO_INCREMENT PRIMARY KEY,
    fkEmpresa INT NOT NULL,
    status TINYINT NOT NULL,
    codigo VARCHAR(45) NOT NULL UNIQUE,
    dataCriacao DATE NOT NULL,
    FOREIGN KEY (fkEmpresa) REFERENCES empresa(idEmpresa)
);

CREATE TABLE reclamacoes (
    id INT PRIMARY KEY AUTO_INCREMENT, 
    uf VARCHAR(2),
    cidade VARCHAR(255),
    data_abertura DATE,
    data_hora_resposta TIMESTAMP,
    data_finalizacao DATE,
    tempo_resposta INT,
    nome_fantasia VARCHAR(255),
    assunto VARCHAR(255),
    grupo_problema VARCHAR(255),
    problema TEXT,
    forma_contrato VARCHAR(255),
    respondida VARCHAR(50),
    situacao VARCHAR(100),
    avaliacao VARCHAR(100),
    nota_consumidor INT,
    codigo_anac VARCHAR(50),
    fkEmpresa int,
    FOREIGN KEY (fkEmpresa) REFERENCES empresa(idEmpresa)
);

INSERT INTO empresa (cnpj, nomeFantasia, razaoSocial) VALUES
('02012862000160','latam airlines (tam)','tam linhas aereas s/a.'),
('07575651000159','gol linhas aereas','gol linhas aereas s.a.'),
('09296295000160','azul linhas aereas','azul linhas aereas brasileiras s.a.'),
('00512777000149','voepass linhas aereas','passaredo transportes aereos s.a.'),
('45153382000121','italia trasporto aereo (ita airways)','italia trasporto aereo s.p.a.'),
('05385049000123','air canada','air canada'),
('02286477000100','air china','air china'),
('02204537000107','air europa','air europa lineas aereas sociedad anonima'),
('33013988000115','air france','societe air france'),
('33605239000100','aerolineas argentinas','aerolineas argentinas sa'),
('01369588000110','aeromexico','aerovias de mexico s/a de c v aeromexico'),
('36212637000199','american airlines','american airlines inc.'),
('21214807000105','amaszonas linea aerea','compania de servicios de transporte aereo amaszonas s/a'),
('51080286000101','arajet s.a.','arajet s.a.'),
('33712837000112','avianca - voos internacionais','aerovias del continente americano s.a. avianca'),
('50710730000154','british airways','british airways plc'),
('12357791000190','boliviana de aviacion - boa','boliviana de aviacion - boa'),
('06980054000147','cabo verde airlines','empresa de transportes aereos de cabo verde tacv s/a'),
('03834757000185','copa airlines','compania panamena de aviacion s/a'),
('00146461000177','delta air lines','delta air lines inc'),
('24494325000136','edelweiss air','edelweiss air ag'),
('33871534000142','el al','el al israel airlines ltd'),
('08692080000103','emirates','emirates'),
('48012345000178','ethiopian airlines','ethiopian airlines enterprise'),
('33143271000155','flybondi','fb lineas aereas s.a.'),
('13115840000143','iberia lineas aereas','iberia lineas aereas de espana sociedad anonima operadora'),
('35184890000113','jetsmart airlines - argentina','jetsmart airlines s.a.'),
('40170083000181','jetsmart airlines - chile','jetsmart airlines ltda'),
('33643420000145','klm','klm – cia. real holandesa de aviacao'),
('33461740000184','lufthansa','deutsche lufthansa ag'),
('10483635000140','map linhas aereas','map transportes aereos ltda.'),
('08734301000150','qatar airways','qatar airways group'),
('42564187000104','royal air maroc','compagnie nationale royalair maroc'),
('48123456000178','sky airline','sky airline'),
('33896614000152','south african airways','south african airways state owned company'),
('04489027000140','surinam airways','surinam airways ltda'),
('38001002000134','swiss','swiss international air lines ag'),
('29926961000103','taag - linhas aereas de angola','taag linhas aereas de angola'),
('33136896000190','tap air portugal','transportes aereos portugueses sa'),
('10576103000158','turkish airlines','turkish airlines inc.'),
('01526415000166','united airlines','united airlines, inc.'),
('48567890000151','virgin atlantic airways','virgin atlantic airways limited');

INSERT INTO usuario (fkEmpresa, nome, cpf, email, senha, cargo) VALUES
(1, 'Ana Silva', '12345678901', 'ana.silva@airwise.com', md5('senhaForte123'), 'Gerente de TI'),
(1, 'Joao Ribeiro', '1235367890', 'joao.ribeiro@airwise.com', md5('AOlcakcsnoj234243'), 'Analista');

INSERT INTO endereco (fkEmpresa, cep, logradouro, numero, bairro, cidade, uf) VALUES
(1, '01311-000', 'Avenida Paulista', 1578, 'Bela Vista', 'São Paulo', 'SP'),
(2, '20090-003', 'Avenida Rio Branco', 1, 'Centro', 'Rio de Janeiro', 'RJ');

INSERT INTO chaveDeAcesso (fkEmpresa, status, codigo, dataCriacao) VALUES
(1, 1, 'AERO-TECH-KEY-2025-ACTIVE', '2025-01-15'),
(2, 0, 'SKY-HIGH-KEY-2024-INACTIVE', '2024-11-20');