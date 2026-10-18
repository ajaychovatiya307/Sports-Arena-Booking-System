module.exports = {
  testEnvironment: 'jsdom',
  transform: { '^.+\\.(t|j)sx?$': 'babel-jest' },
  moduleNameMapper: {
    '\\.(css|less|scss)$': 'identity-obj-proxy'
  },
  setupFilesAfterEnv: ['<rootDir>/jest.setup.ts'],
  testMatch: ['**/__tests__/**/*.test.ts?(x)'],
  collectCoverage: true,
  coverageDirectory: 'coverage',
  coverageReporters: ['lcov', 'text'],
  collectCoverageFrom: ['src/**/*.{ts,tsx}', '!src/**/__tests__/**', '!src/main.tsx', '!src/types.ts']
};
