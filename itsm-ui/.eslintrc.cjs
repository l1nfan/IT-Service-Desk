module.exports = {
    root: true,
    env: {
        browser: true,
        node: true,
        es2022: true,
    },
    extends: [
        'eslint:recommended',
    ],
    parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
    },
    rules: {
        // Error prevention
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
        'no-undef': 'error',

        // Best practices
        'eqeqeq': ['warn', 'always'],
        'no-var': 'error',
        'prefer-const': 'warn',
        'no-multiple-empty-lines': ['warn', { max: 1 }],
        'no-trailing-spaces': 'warn',
        'comma-dangle': ['warn', 'always-multiline'],
        'quotes': ['warn', 'single', { avoidEscape: true }],
        'semi': ['warn', 'always'],

        // Vue-specific (applied via .vue files handled by Vite/Vue plugin)
        'vue/multi-word-component-names': 'off',
    },
    globals: {
        defineProps: 'readonly',
        defineEmits: 'readonly',
        defineExpose: 'readonly',
        withDefaults: 'readonly',
    },
};
