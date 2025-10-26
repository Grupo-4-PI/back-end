#!/bin/bash
set -e

echo "Iniciando a configuração completa do servidor e da aplicação..."

echo "Atualizando pacotes do sistema..."
sudo apt-get update && sudo apt-get upgrade -y

if ! command -v docker &> /dev/null
then
    echo "Instalando o Docker Engine e o Compose Plugin usando o script de conveniência..."
    curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh
    rm get-docker.sh 
    echo "Docker instalado com sucesso."
else
    echo "Docker já está instalado."
fi

echo "Ativando e habilitando o serviço Docker..."
sudo systemctl start docker
sudo systemctl enable docker

echo "-->Iniciando a aplicação com Docker Compose..."
sudo docker compose -f infraestrutura/docker-compose.yml up --build -d

echo "--> Carregando/Atualizando o agendamento do cron..."
sudo crontab /home/ubuntu/infraestrutura/trabalho_agendado.cron
echo "Agendamento do cron carregado a partir de trabalho_agendado.cron"

echo "" 
echo ">>> PROCESSO FINALIZADO <<<"
echo "Aplicação iniciada."
echo "Use 'sudo docker compose -f infraestrutura/docker-compose.yml ps' para verificar o status."