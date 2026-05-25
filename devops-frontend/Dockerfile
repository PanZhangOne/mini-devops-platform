FROM node:22-alpine AS builder

WORKDIR /repo

RUN corepack enable

COPY package.json pnpm-lock.yaml tsconfig.json eslint.config.js index.html tsconfig.node.json tsconfig.app.json vite.config.ts ./
COPY src /repo/src

RUN pnpm install --frozen-lockfile

RUN pnpm build

FROM nginx:alpine as runner

COPY --from=builder /repo/dist /usr/share/nginx/html

# 刷新页面时强制回到index.html，适用于 SPA 应用
RUN printf '%s\n' \
    'server {' \
    '    listen 80;' \
    '    server_name _;' \
    '    root /usr/share/nginx/html;' \
    '    index index.html;' \
    '' \
    '    location / {' \
    '        try_files $uri $uri/ /index.html;' \
    '    }' \
    '}' \
    > /etc/nginx/conf.d/default.conf


EXPOSE 80

CMD ["nginx", "-g", "daemon off"]