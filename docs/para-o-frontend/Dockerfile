# Dockerfile do frontend. Vai na RAIZ do repositorio pi4-0102-g7-frontend.
#
# So o Compose de apresentacao usa este arquivo:
#   docker compose -f docker-compose.prod.yml up --build
# O Compose do dia a dia nao precisa dele (usa a imagem do Node direto).

# Etapa 1: compilar o React.
FROM node:20-alpine AS compilacao
WORKDIR /app
# Copia so os arquivos de dependencia primeiro: assim o Docker reaproveita
# o "npm ci" quando muda apenas o codigo das telas.
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: servir os arquivos prontos pelo nginx.
# A imagem final nao tem Node nem node_modules, so os arquivos compilados.
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacao /app/dist /usr/share/nginx/html
EXPOSE 80
