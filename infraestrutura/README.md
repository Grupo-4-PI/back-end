# Intrução para utilização

## Config .env
- `Atualize todas as variaveis de ambiente AWS do .env com suas credenciais todas vez que logar em sua EC2`

## Config EC2
- `Crie uma EC2 com o t2lager`
- `Habilite o IAM de sua EC2`
- `Atulize os grupos e segurança para a utilização de portas públicas`

## ** Faca as altercoes no comando abaixo para que tudo funcione **

## Execute este comando para clonar este arquivo para sua EC2
- `scp -i "" -r  ubuntu@54.145.214.219:~/`

## Dê a permissão para executar o arquivo
- `chmod +x infraestrutura/setup.sh`

## Execute o arquivo
- `sudo ./infraestrutura/setup.sh`

## Acessando os Serviços
- **Front-end (Node.js):** `http://<IP_DO_SERVIDOR>:3333`
- **Back-end (Java/JAR):** `http://<IP_DO_SERVIDOR>:8080`