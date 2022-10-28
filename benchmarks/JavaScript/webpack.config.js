const path = require('path');

const es5 = {
  mode: 'production',
  optimization: {
    minimize: false
  },
  entry: './harness.js',
  output: {
    filename: 'harness.es5.js',
    path: path.resolve(__dirname, 'dist'),
    chunkFormat: 'commonjs'
  },
  module: {
    rules: [
      {
        test: /\.m?js$/,
        exclude: /(node_modules|bower_components)/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: ['@babel/preset-env']
          }
        }
      }
    ]
  },
  target: 'es5'
};

const es2022 = {
  mode: 'production',
  optimization: {
    minimize: false
  },
  entry: './harness.js',
  output: {
    filename: 'harness.es2022.js',
    path: path.resolve(__dirname, 'dist'),
    chunkFormat: 'commonjs'
  },
  target: 'es2022'
};

module.exports = [es5, es2022];
